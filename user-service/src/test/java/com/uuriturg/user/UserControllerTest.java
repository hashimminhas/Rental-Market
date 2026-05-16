package com.uuriturg.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uuriturg.user.controller.UserController;
import com.uuriturg.user.dto.*;
import com.uuriturg.user.exception.DuplicateEmailException;
import com.uuriturg.user.exception.UserNotFoundException;
import com.uuriturg.user.service.SavedSearchService;
import com.uuriturg.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean UserService userService;
    @MockitoBean SavedSearchService savedSearchService;

    private static final UUID USER_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

    private UserResponse buildUserResponse() {
        return UserResponse.builder()
                .userId(USER_ID)
                .firstName("Jaan")
                .lastName("Tamm")
                .email("jaan.tamm@ut.ee")
                .phone("+372 5123 4567")
                .role("TENANT")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ─── Happy path ────────────────────────────────────────────────────────────

    @Test
    void createUser_returns200_withUserResponse() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("Jaan").lastName("Tamm")
                .email("jaan.tamm@ut.ee").role("TENANT").build();

        when(userService.createUser(any())).thenReturn(buildUserResponse());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.email").value("jaan.tamm@ut.ee"))
                .andExpect(jsonPath("$.role").value("TENANT"));
    }

    @Test
    void getUserById_returns200_withFullProfile() throws Exception {
        when(userService.getUserById(USER_ID)).thenReturn(buildUserResponse());

        mockMvc.perform(get("/users/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jaan"))
                .andExpect(jsonPath("$.lastName").value("Tamm"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void validateUser_returns200_withActiveUser() throws Exception {
        ValidateUserResponse validate = ValidateUserResponse.builder()
                .userId(USER_ID)
                .email("jaan.tamm@ut.ee")
                .role("TENANT")
                .active(true)
                .build();

        when(userService.validateUser(USER_ID)).thenReturn(validate);

        mockMvc.perform(get("/users/validate/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.role").value("TENANT"));
    }

    @Test
    void deleteUser_returns204() throws Exception {
        doNothing().when(userService).deleteUser(USER_ID);

        mockMvc.perform(delete("/users/{id}", USER_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void getSavedSearches_returns200_withList() throws Exception {
        SavedSearchResponse search = SavedSearchResponse.builder()
                .searchId(UUID.randomUUID())
                .userId(USER_ID)
                .neighborhood("Kesklinn")
                .maxPrice(new BigDecimal("600"))
                .minRooms(2)
                .createdAt(LocalDateTime.now())
                .build();

        when(savedSearchService.getSearchesForUser(USER_ID)).thenReturn(List.of(search));

        mockMvc.perform(get("/users/{id}/searches", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].neighborhood").value("Kesklinn"));
    }

    @Test
    void saveSearch_returns200_withSavedSearch() throws Exception {
        SavedSearchRequest request = SavedSearchRequest.builder()
                .neighborhood("Annelinn")
                .maxPrice(new BigDecimal("500"))
                .build();

        SavedSearchResponse response = SavedSearchResponse.builder()
                .searchId(UUID.randomUUID())
                .userId(USER_ID)
                .neighborhood("Annelinn")
                .maxPrice(new BigDecimal("500"))
                .createdAt(LocalDateTime.now())
                .build();

        when(savedSearchService.saveSearch(eq(USER_ID), any())).thenReturn(response);

        mockMvc.perform(post("/users/{id}/searches", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.neighborhood").value("Annelinn"))
                .andExpect(jsonPath("$.maxPrice").value(500));
    }

    // ─── Error cases ───────────────────────────────────────────────────────────

    @Test
    void getUserById_returns404_whenNotFound() throws Exception {
        UUID unknown = UUID.randomUUID();
        when(userService.getUserById(unknown)).thenThrow(new UserNotFoundException(unknown));

        mockMvc.perform(get("/users/{id}", unknown))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void createUser_returns409_whenEmailDuplicate() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("Jaan").lastName("Tamm")
                .email("taken@ut.ee").build();

        when(userService.createUser(any()))
                .thenThrow(new DuplicateEmailException("taken@ut.ee"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void validateUser_returns404_whenUserInactive() throws Exception {
        UUID unknown = UUID.randomUUID();
        when(userService.validateUser(unknown)).thenThrow(new UserNotFoundException(unknown));

        mockMvc.perform(get("/users/validate/{id}", unknown))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
