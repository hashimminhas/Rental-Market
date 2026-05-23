<template>
  <div class="page">
    <div class="page-hd">
      <div>
        <h1 class="page-title">Landlords</h1>
        <p class="page-sub">Verified landlord profiles and their listed properties.</p>
      </div>
      <button class="btn btn-primary btn-sm" @click="showForm=true">
        <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg>
        Add landlord
      </button>
    </div>

    <!-- Filters -->
    <div class="card" style="padding:14px 20px">
      <div style="display:flex;align-items:center;gap:10px;flex-wrap:wrap">
        <div class="fsearch">
          <svg width="14" height="14" fill="none" stroke="#94a3b8" stroke-width="2" viewBox="0 0 24 24" class="fsi"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
          <input v-model="search" class="finput" placeholder="Search landlords…" />
        </div>
        <select v-model="fVerified" class="form-select" style="width:160px">
          <option value="">All landlords</option>
          <option value="true">Verified only</option>
          <option value="false">Unverified only</option>
        </select>
        <select v-model="sortBy" class="form-select" style="width:160px">
          <option value="name">Name A–Z</option>
          <option value="newest">Newest first</option>
          <option value="listings">Most listings</option>
        </select>
      </div>
    </div>

    <div v-if="loading" class="card" style="padding:60px">
      <div class="state-loading" style="padding:0"><div class="spinner"></div><span>Loading landlords…</span></div>
    </div>
    <div v-else-if="displayed.length===0" class="card" style="padding:60px">
      <div class="state-empty" style="padding:0">
        <svg width="36" height="36" fill="none" stroke="#94a3b8" stroke-width="1.5" viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
        <p>No landlords found.</p>
        <button class="btn btn-outline btn-sm" @click="showForm=true">Add the first landlord</button>
      </div>
    </div>
    <div v-else class="ll-grid">
      <div v-for="l in displayed" :key="l.landlordId" class="llcard">
        <div class="llcard-hd">
          <div class="ll-avatar">{{ initials(l) }}</div>
          <div style="flex:1;min-width:0">
            <div class="ll-name">
              {{ l.name||l.contactName||'Landlord' }}
              <svg v-if="l.verified" width="13" height="13" fill="#0d9488" viewBox="0 0 24 24" title="Verified"><path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
            </div>
            <div class="ll-joined" v-if="l.createdAt">Joined {{ fmtDate(l.createdAt) }}</div>
          </div>
          <div style="display:flex;gap:4px">
            <button class="btn btn-icon" @click="startEdit(l)" title="Edit">
              <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
            </button>
            <button class="btn btn-icon" @click="del(l)" title="Delete" style="color:#dc2626">
              <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6M9 6V4h6v2"/></svg>
            </button>
          </div>
        </div>
        <div class="ll-badges">
          <span :class="['badge', l.verified?'badge-green':'badge-gray']">{{ l.verified?'Verified':'Unverified' }}</span>
          <span v-if="l.listingCount>0" class="badge badge-blue">{{ l.listingCount }} listing{{ l.listingCount===1?'':'s' }}</span>
        </div>
        <div v-if="l.phone||l.email" class="ll-contacts">
          <div v-if="l.phone" class="ll-contact">
            <svg width="11" height="11" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07A19.5 19.5 0 013.07 9.62 19.79 19.79 0 01.12 1a2 2 0 012-2.18h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L6.91 6.91a16 16 0 006.18 6.18l1.28-1.28a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 16.92z"/></svg>
            {{ l.phone }}
          </div>
          <div v-if="l.email" class="ll-contact">
            <svg width="11" height="11" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
            {{ l.email }}
          </div>
        </div>
        <div v-if="l.responseRate!=null" class="ll-rr">
          <span class="ll-rr-lbl">Response rate</span>
          <div class="ll-rr-bar"><div class="ll-rr-fill" :style="{width:l.responseRate+'%',background:rrColor(l.responseRate)}"></div></div>
          <span class="ll-rr-val">{{ l.responseRate }}%</span>
        </div>
      </div>
    </div>

    <!-- Create / Edit modal -->
    <div v-if="showForm||editTarget" class="modal-ov" @click.self="closeModal">
      <div class="modal">
        <div class="modal-hd">
          <span class="modal-title">{{ editTarget?'Edit landlord':'Add landlord' }}</span>
          <button class="btn btn-icon" @click="closeModal">
            <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M18 6L6 18M6 6l12 12"/></svg>
          </button>
        </div>
        <div style="display:flex;flex-direction:column;gap:13px">
          <div class="form-group">
            <label class="form-label">Name *</label>
            <input v-model="f.name" class="form-input" placeholder="Full name or company" required />
          </div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px">
            <div class="form-group">
              <label class="form-label">Email</label>
              <input v-model="f.email" type="email" class="form-input" placeholder="landlord@example.com" />
            </div>
            <div class="form-group">
              <label class="form-label">Phone</label>
              <input v-model="f.phone" class="form-input" placeholder="+372 5xxx xxxx" />
            </div>
          </div>
          <div style="display:flex;align-items:center;gap:12px">
            <label class="form-label" style="margin:0">Verified</label>
            <label class="toggle">
              <input type="checkbox" v-model="f.verified" />
              <span class="toggle-track"><span class="toggle-thumb"></span></span>
            </label>
          </div>
          <div v-if="formErr" style="color:#dc2626;font-size:.82rem">{{ formErr }}</div>
          <div style="display:flex;gap:10px;justify-content:flex-end;padding-top:4px">
            <button type="button" class="btn btn-ghost btn-sm" @click="closeModal">Cancel</button>
            <button type="button" class="btn btn-primary" :disabled="saving" @click="saveLL">{{ saving?'Saving…':'Save' }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const landlords  = ref([])
const loading    = ref(true)
const saving     = ref(false)
const search     = ref('')
const fVerified  = ref('')
const sortBy     = ref('name')
const showForm   = ref(false)
const editTarget = ref(null)
const formErr    = ref('')

const blank = () => ({name:'',email:'',phone:'',verified:false})
const f = ref(blank())

const displayed = computed(() => {
  let a = landlords.value
  if(search.value){ const q=search.value.toLowerCase(); a=a.filter(l=>(l.name||l.contactName||'').toLowerCase().includes(q)||(l.email||'').toLowerCase().includes(q)) }
  if(fVerified.value!=='') a=a.filter(l=>String(l.verified)===fVerified.value)
  switch(sortBy.value){
    case 'newest':   return [...a].sort((x,y)=>new Date(y.createdAt||0)-new Date(x.createdAt||0))
    case 'listings': return [...a].sort((x,y)=>(y.listingCount||0)-(x.listingCount||0))
    default:         return [...a].sort((x,y)=>(x.name||'').localeCompare(y.name||''))
  }
})

function initials(l){ const n=l.name||l.contactName||'?'; return n.split(' ').map(w=>w[0]).slice(0,2).join('').toUpperCase() }
function fmtDate(d){ return new Date(d).toLocaleDateString('et-EE',{month:'short',year:'numeric'}) }
function rrColor(v){ return v>=80?'#16a34a':v>=50?'#f59e0b':'#dc2626' }

async function load(){
  loading.value=true
  try{ const r=await fetch('/api/landlords'); if(r.ok){ const d=await r.json(); landlords.value=Array.isArray(d)?d:(d.content||[]) } }
  catch{} finally{ loading.value=false }
}

function startEdit(l){ editTarget.value=l.landlordId; f.value={name:l.name||l.contactName||'',email:l.email||'',phone:l.phone||'',verified:!!l.verified} }
function closeModal(){ showForm.value=false; editTarget.value=null; f.value=blank(); formErr.value='' }

async function saveLL(){
  if(!f.value.name.trim()){ formErr.value='Name is required'; return }
  saving.value=true; formErr.value=''
  const body={name:f.value.name,contactName:f.value.name,email:f.value.email||null,phone:f.value.phone||null,verified:f.value.verified}
  try{
    const url = editTarget.value ? `/api/landlords/${editTarget.value}` : '/api/landlords'
    const method = editTarget.value ? 'PUT' : 'POST'
    const r = await fetch(url,{method,headers:{'Content-Type':'application/json'},body:JSON.stringify(body)})
    if(r.ok){ closeModal(); await load() }
    else formErr.value='Failed (HTTP '+r.status+')'
  }catch{ formErr.value='Network error.' }
  finally{ saving.value=false }
}

async function del(l){
  if(!confirm(`Delete "${l.name||'this landlord'}"?`)) return
  try{ await fetch(`/api/landlords/${l.landlordId}`,{method:'DELETE'}); await load() }catch{}
}

onMounted(load)
</script>

<style scoped>
.page       { display:flex; flex-direction:column; gap:18px; }
.page-hd    { display:flex; justify-content:space-between; align-items:flex-start; }
.page-title { font-size:1.375rem; font-weight:700; color:var(--text); margin:0 0 4px; }
.page-sub   { font-size:.875rem; color:var(--muted); margin:0; }

.fsearch { position:relative; flex:1; min-width:200px; }
.fsi     { position:absolute; left:10px; top:50%; transform:translateY(-50%); pointer-events:none; }
.finput  { width:100%; padding:7px 10px 7px 32px; border:1px solid var(--border); border-radius:var(--r); font-size:.85rem; color:var(--text); background:var(--bg); box-sizing:border-box; }
.finput:focus { outline:none; border-color:var(--primary); box-shadow:0 0 0 3px rgba(13,148,136,.12); }

.ll-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(270px,1fr)); gap:16px; }

.llcard {
  background:var(--card); border:1px solid var(--border); border-radius:var(--r);
  padding:18px; box-shadow:var(--shadow); display:flex; flex-direction:column; gap:12px;
  transition:box-shadow .2s,border-color .2s;
}
.llcard:hover { box-shadow:0 4px 16px rgba(0,0,0,.1); border-color:var(--primary); }

.llcard-hd { display:flex; align-items:center; gap:12px; }
.ll-avatar { width:44px; height:44px; border-radius:50%; background:var(--primary-light); color:var(--primary); font-weight:700; font-size:.95rem; display:flex; align-items:center; justify-content:center; flex-shrink:0; }
.ll-name   { font-size:.9rem; font-weight:600; color:var(--text); display:flex; align-items:center; gap:5px; }
.ll-joined { font-size:.75rem; color:var(--muted); }
.ll-badges { display:flex; flex-wrap:wrap; gap:6px; }

.ll-contacts { display:flex; flex-direction:column; gap:4px; }
.ll-contact  { display:flex; align-items:center; gap:6px; font-size:.8rem; color:var(--muted); }

.ll-rr     { display:flex; align-items:center; gap:8px; }
.ll-rr-lbl { font-size:.75rem; color:var(--muted); white-space:nowrap; }
.ll-rr-bar { flex:1; height:5px; background:var(--border); border-radius:3px; overflow:hidden; }
.ll-rr-fill{ height:100%; border-radius:3px; transition:width .4s; }
.ll-rr-val { font-size:.75rem; font-weight:600; color:var(--text); }

.modal-ov  { position:fixed; inset:0; background:rgba(0,0,0,.4); display:flex; align-items:center; justify-content:center; z-index:200; }
.modal     { background:#fff; border-radius:var(--r); padding:24px; min-width:400px; max-width:95vw; box-shadow:0 20px 60px rgba(0,0,0,.2); }
.modal-hd  { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; }
.modal-title { font-size:1rem; font-weight:700; color:var(--text); }
</style>
