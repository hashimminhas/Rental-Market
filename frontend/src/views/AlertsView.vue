<template>
  <div class="page">
    <div class="page-hd">
      <div>
        <h1 class="page-title">Price Alerts</h1>
        <p class="page-sub">Get notified when new listings match your criteria.</p>
      </div>
    </div>

    <div class="alerts-layout">

      <!-- Create / Edit form -->
      <div class="card form-card">
        <div class="form-card-title">{{ editId ? 'Edit alert' : 'Create new alert' }}</div>
        <form @submit.prevent="save">
          <div class="form-group">
            <label class="form-label">Your email *</label>
            <input v-model="f.email" type="email" class="form-input" placeholder="you@example.com" required />
            <div class="form-hint">We'll email you when a match is found. No account needed.</div>
          </div>
          <div class="form-group">
            <label class="form-label">Alert name</label>
            <input v-model="f.name" class="form-input" placeholder="e.g. Budget flat in Karlova" />
          </div>
          <div class="form-group">
            <label class="form-label">Neighborhood</label>
            <select v-model="f.neighborhood" class="form-select">
              <option value="">Any neighborhood</option>
              <option v-for="n in HOODS" :key="n" :value="n">{{ n }}</option>
            </select>
          </div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px">
            <div class="form-group">
              <label class="form-label">Min price (€)</label>
              <input v-model.number="f.minPrice" type="number" class="form-input" placeholder="0" min="0" />
            </div>
            <div class="form-group">
              <label class="form-label">Max price (€)</label>
              <input v-model.number="f.maxPrice" type="number" class="form-input" placeholder="1500" min="0" />
            </div>
          </div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px">
            <div class="form-group">
              <label class="form-label">Min size (m²)</label>
              <input v-model.number="f.minSize" type="number" class="form-input" placeholder="0" min="0" />
            </div>
            <div class="form-group">
              <label class="form-label">Min rooms</label>
              <select v-model.number="f.minRooms" class="form-select">
                <option :value="null">Any</option>
                <option :value="1">1+</option>
                <option :value="2">2+</option>
                <option :value="3">3+</option>
                <option :value="4">4+</option>
              </select>
            </div>
          </div>
          <div v-if="formErr" style="color:#dc2626;font-size:.82rem;margin-bottom:8px">{{ formErr }}</div>
          <div v-if="formOk"  style="color:#16a34a;font-size:.82rem;margin-bottom:8px">{{ formOk }}</div>
          <div style="display:flex;gap:8px;justify-content:flex-end">
            <button v-if="editId" type="button" class="btn btn-ghost btn-sm" @click="cancelEdit">Cancel</button>
            <button type="submit" class="btn btn-primary" :disabled="saving">{{ saving?'Saving…':editId?'Update alert':'Create alert' }}</button>
          </div>
        </form>
      </div>

      <!-- Alert list -->
      <div class="alerts-col">
        <div v-if="loading" class="card" style="padding:40px">
          <div class="state-loading" style="padding:0"><div class="spinner"></div><span>Loading alerts…</span></div>
        </div>
        <div v-else-if="alerts.length===0" class="card" style="padding:48px">
          <div class="state-empty" style="padding:0">
            <svg width="36" height="36" fill="none" stroke="#94a3b8" stroke-width="1.5" viewBox="0 0 24 24"><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/></svg>
            <p>No alerts yet. Create your first rule!</p>
          </div>
        </div>
        <div v-else class="alert-list">
          <div v-for="a in alerts" :key="a.alertId" :class="['acard', !a.isActive&&'acard--off']">
            <div class="acard-hd">
              <span class="acard-name">{{ a.name||'Alert' }}</span>
              <div style="display:flex;align-items:center;gap:6px">
                <label class="toggle" :title="a.isActive?'Disable':'Enable'">
                  <input type="checkbox" :checked="a.isActive" @change="toggleAlert(a)"/>
                  <span class="toggle-track"><span class="toggle-thumb"></span></span>
                </label>
                <button class="btn btn-icon" @click="startEdit(a)" title="Edit">
                  <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                </button>
                <button class="btn btn-icon" @click="del(a)" title="Delete" style="color:#dc2626">
                  <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6M9 6V4h6v2"/></svg>
                </button>
              </div>
            </div>
            <div class="acard-chips">
              <span class="chip" v-if="a.neighborhood">{{ a.neighborhood }}</span>
              <span class="chip" v-if="a.minPrice||a.maxPrice">€{{ a.minPrice||0 }} – {{ a.maxPrice?'€'+a.maxPrice:'∞' }}</span>
              <span class="chip" v-if="a.minSize">≥ {{ a.minSize }} m²</span>
              <span class="chip" v-if="a.minRooms">{{ a.minRooms }}+ rooms</span>
              <span class="chip" v-if="!a.neighborhood&&!a.minPrice&&!a.maxPrice&&!a.rooms">All listings</span>
            </div>
            <div class="acard-ft">
              <span v-if="a.email" style="display:flex;align-items:center;gap:5px;font-size:.78rem;color:var(--muted)">
                <svg width="11" height="11" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
                {{ a.email }}
              </span>
              <span :class="['badge', a.isActive?'badge-green':'badge-gray']">{{ a.isActive?'Active':'Paused' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const HOODS = ['Kesklinn','Ülejõe','Tammelinn','Annelinn','Karlova','Veeriku','Tähtvere','Supilinn','Ränilinn','Maarjamõisa']

const alerts  = ref([])
const loading = ref(true)
const saving  = ref(false)
const editId  = ref(null)
const formErr = ref('')
const formOk  = ref('')

const blank = () => ({name:'',email:'',neighborhood:'',minPrice:null,maxPrice:null,minSize:null,minRooms:null})
const f = ref(blank())

async function load(){
  loading.value=true
  try{ const r=await fetch('/api/alerts'); if(r.ok) alerts.value=await r.json() }
  catch{} finally{ loading.value=false }
}

async function save(){
  saving.value=true; formErr.value=''; formOk.value=''
  const body={email:f.value.email,name:f.value.name||null,neighborhood:f.value.neighborhood||null,minPrice:f.value.minPrice||null,maxPrice:f.value.maxPrice||null,minSize:f.value.minSize||null,minRooms:f.value.minRooms||null}
  try{
    const url = editId.value ? `/api/alerts/${editId.value}` : '/api/alerts'
    const method = editId.value ? 'PUT' : 'POST'
    const r = await fetch(url,{method,headers:{'Content-Type':'application/json'},body:JSON.stringify(body)})
    if(r.ok){ formOk.value=editId.value?'Alert updated!':'Alert created!'; f.value=blank(); editId.value=null; await load(); setTimeout(()=>{formOk.value=''},3000) }
    else formErr.value='Failed (HTTP '+r.status+')'
  }catch{ formErr.value='Network error — is the alert service running?' }
  finally{ saving.value=false }
}

function startEdit(a){
  editId.value=a.alertId
  f.value={email:a.email||'',name:a.name||'',neighborhood:a.neighborhood||'',minPrice:a.minPrice,maxPrice:a.maxPrice,minSize:a.minSize,minRooms:a.minRooms}
}
function cancelEdit(){ editId.value=null; f.value=blank(); formErr.value='' }

async function toggleAlert(a){
  try{ await fetch(`/api/alerts/${a.alertId}`,{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify({...a,isActive:!a.isActive})}); await load() }catch{}
}
async function del(a){
  if(!confirm(`Delete alert "${a.name||'Alert'}"?`)) return
  try{ await fetch(`/api/alerts/${a.alertId}`,{method:'DELETE'}); await load() }catch{}
}

onMounted(load)
</script>

<style scoped>
.page       { display:flex; flex-direction:column; gap:18px; }
.page-hd    { display:flex; justify-content:space-between; align-items:flex-start; }
.page-title { font-size:1.375rem; font-weight:700; color:var(--text); margin:0 0 4px; }
.page-sub   { font-size:.875rem; color:var(--muted); margin:0; }

.alerts-layout { display:grid; grid-template-columns:330px 1fr; gap:20px; align-items:start; }
@media(max-width:860px){ .alerts-layout { grid-template-columns:1fr; } }

.form-card       { padding:22px; }
.form-card-title { font-size:1rem; font-weight:600; color:var(--text); margin-bottom:18px; }
.form-hint       { font-size:.75rem; color:var(--muted); margin-top:4px; }

.alerts-col { display:flex; flex-direction:column; gap:0; }
.alert-list { display:flex; flex-direction:column; gap:12px; }

.acard { background:var(--card); border:1px solid var(--border); border-radius:var(--r); padding:16px; box-shadow:var(--shadow); transition:border-color .2s; }
.acard:hover { border-color:var(--primary); }
.acard--off  { opacity:.6; }

.acard-hd    { display:flex; justify-content:space-between; align-items:center; margin-bottom:10px; }
.acard-name  { font-size:.9rem; font-weight:600; color:var(--text); }
.acard-chips { display:flex; flex-wrap:wrap; gap:6px; margin-bottom:10px; }
.acard-ft    { display:flex; align-items:center; justify-content:space-between; }
.chip { font-size:.75rem; background:var(--primary-light); color:var(--primary-dark); padding:2px 8px; border-radius:20px; font-weight:500; }
</style>
