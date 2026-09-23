<template>
  <div class="ai-assistant">
    <el-card class="chat-card" body-class="chat-body">
      <template #header>
        <div class="chat-header">
          <div class="title">
            <el-icon color="#409EFF"><MagicStick /></el-icon>
            <span>实验室管理系统 · 智能助手</span>
          </div>
          <div>
            <el-button size="small" plain @click="clearSession">清空会话</el-button>
            <el-button size="small" type="primary" plain @click="newSession">新建会话</el-button>
          </div>
        </div>
      </template>

      <el-alert type="info" :closable="false" show-icon class="scope-tip"
        title="本助手仅回答与「实验室管理系统」的功能、操作、数据、权限、流程相关的问题；无关问题会被拒绝。" />

      <div ref="scrollRef" class="msg-list" v-loading="loadingHistory">
        <div v-if="messages.length === 0" class="welcome">
          <div class="welcome-title">你好，我可以帮你了解这个实验室管理系统</div>
          <div class="welcome-sub">点击下面的示例快速提问：</div>
          <div class="suggests">
            <el-tag v-for="s in suggestions" :key="s" class="suggest-tag" effect="plain" @click="ask(s)">{{ s }}</el-tag>
          </div>
        </div>

        <div v-for="(m, idx) in messages" :key="idx" :class="['row', m.role]">
          <el-avatar :size="30" :class="['avatar', m.role]">
            <el-icon><component :is="m.role === 'user' ? 'User' : 'MagicStick'" /></el-icon>
          </el-avatar>
          <div class="bubble-wrap">
            <div :class="['bubble', m.role, { refusal: m.outOfScope }]">{{ m.content }}</div>
            <div v-if="m.role === 'assistant' && m.refs && m.refs.length" class="refs">
              <span class="refs-label">参考：</span>
              <el-tag v-for="r in m.refs" :key="r.id" size="small" type="info" class="ref-tag">{{ r.category }} · {{ r.question }}</el-tag>
            </div>
            <div v-if="m.outOfScope" class="refusal-flag">该问题超出本系统范围，已按规则拒绝回答</div>
          </div>
        </div>

        <div v-if="sending" class="row assistant">
          <el-avatar :size="30" class="avatar assistant"><el-icon><MagicStick /></el-icon></el-avatar>
          <div class="bubble assistant typing">正在思考…</div>
        </div>
      </div>

      <div class="input-bar">
        <el-input v-model="input" type="textarea" :rows="2" resize="none"
                  placeholder="请输入与本系统相关的问题，Enter 发送 / Shift+Enter 换行"
                  @keydown="onKeydown" />
        <el-button type="primary" :loading="sending" class="send-btn" @click="send()">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const SESSION_KEY = 'aiSessionId'
const suggestions = [
  '这个系统都有哪些功能模块？',
  '如何预约实验室或仪器？',
  '预约的审批流程和状态是怎样的？',
  '耗材怎么领用和归还？',
  '不同角色分别有哪些权限？',
  '数据统计能看到哪些内容？'
]

const messages = ref([])
const input = ref('')
const sending = ref(false)
const loadingHistory = ref(false)
const scrollRef = ref(null)
let sessionId = ''

const persistSession = (id) => { sessionId = id; localStorage.setItem(SESSION_KEY, id) }
const scrollToBottom = () => nextTick(() => { if (scrollRef.value) scrollRef.value.scrollTop = scrollRef.value.scrollHeight })
const parseRefs = (raw) => { if (!raw) return []; try { return JSON.parse(raw) } catch (e) { return [] } }

const loadHistory = async (sid) => {
  if (!sid) { messages.value = []; return }
  loadingHistory.value = true
  try {
    const res = await request.get('/ai/history', { params: { sessionId: sid } })
    if (res.code === 200) {
      messages.value = (res.data || []).map(m => ({ role: m.role, content: m.content, refs: parseRefs(m.refs), outOfScope: false }))
      scrollToBottom()
    }
  } finally { loadingHistory.value = false }
}

const send = async (text) => {
  const q = (typeof text === 'string' ? text : input.value).trim()
  if (!q) { ElMessage.warning('请输入问题'); return }
  if (sending.value) return
  messages.value.push({ role: 'user', content: q, refs: [], outOfScope: false })
  input.value = ''
  sending.value = true
  scrollToBottom()
  try {
    const res = await request.post('/ai/chat', { question: q, sessionId }, { timeout: 60000 })
    if (res.code === 200 && res.data) {
      if (res.data.sessionId) persistSession(res.data.sessionId)
      messages.value.push({ role: 'assistant', content: res.data.answer || '（无回复）', refs: res.data.refs || [], outOfScope: !!res.data.outOfScope })
    } else {
      messages.value.push({ role: 'assistant', content: res.message || '服务异常，请稍后重试', refs: [], outOfScope: false })
    }
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '请求失败：' + (e.message || '网络异常') + '（若为超时请稍后重试）', refs: [], outOfScope: false })
  } finally { sending.value = false; scrollToBottom() }
}

const ask = (s) => send(s)
const onKeydown = (e) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send() } }
const newSession = () => { persistSession(''); messages.value = []; input.value = '' }
const clearSession = async () => {
  if (!sessionId) { messages.value = []; return }
  try {
    const res = await request.delete('/ai/history', { params: { sessionId } })
    if (res.code === 200) { ElMessage.success('已清空本会话记录'); messages.value = []; persistSession('') }
  } catch (e) { /* ignore */ }
}

onMounted(async () => {
  const saved = localStorage.getItem(SESSION_KEY)
  if (saved) { persistSession(saved); await loadHistory(saved) }
})
</script>

<style scoped>
.ai-assistant { height: 100%; }
.chat-card { height: calc(100vh - 140px); display: flex; flex-direction: column; }
:deep(.chat-body) { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.chat-header { display: flex; justify-content: space-between; align-items: center; }
.chat-header .title { display: flex; align-items: center; gap: 8px; font-weight: 600; }
.scope-tip { margin-bottom: 10px; }
.msg-list { flex: 1; overflow-y: auto; padding: 6px 4px; }
.welcome { text-align: center; color: #606266; padding: 30px 10px; }
.welcome-title { font-size: 16px; margin-bottom: 10px; }
.welcome-sub { font-size: 13px; color: #909399; margin-bottom: 12px; }
.suggests { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; }
.suggest-tag { cursor: pointer; }
.row { display: flex; gap: 10px; margin: 12px 0; }
.row.user { flex-direction: row-reverse; }
.bubble-wrap { max-width: 76%; display: flex; flex-direction: column; }
.row.user .bubble-wrap { align-items: flex-end; }
.bubble { padding: 10px 14px; border-radius: 10px; line-height: 1.6; white-space: pre-wrap; word-break: break-word; font-size: 14px; }
.bubble.user { background: #409EFF; color: #fff; }
.bubble.assistant { background: #f4f4f5; color: #303133; }
.bubble.refusal { background: #fdf6ec; color: #b88230; border: 1px solid #faecd8; }
.refs { margin-top: 6px; font-size: 12px; color: #909399; }
.refs-label { margin-right: 4px; }
.ref-tag { margin: 2px 4px 0 0; }
.refusal-flag { margin-top: 6px; font-size: 12px; color: #e6a23c; }
.typing { color: #909399; }
.input-bar { display: flex; gap: 10px; align-items: flex-end; padding-top: 10px; border-top: 1px solid #ebeef5; }
.input-bar :deep(.el-textarea__inner) { box-shadow: none; border: 1px solid #dcdfe6; }
.send-btn { height: 54px; }
.avatar { flex-shrink: 0; }
.avatar.user { background: #409EFF; }
.avatar.assistant { background: #67c23a; }
</style>
