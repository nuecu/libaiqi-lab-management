import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { usePermissionStore } from './stores/permission'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// v-permission 指令：无对应权限码时移除元素（支持字符串或数组=任一命中）
app.directive('permission', {
  mounted(el, binding) {
    const store = usePermissionStore()
    const required = Array.isArray(binding.value) ? binding.value : [binding.value]
    const allowed = required.some(code => store.hasPermission(code))
    if (!allowed && el.parentNode) {
      el.parentNode.removeChild(el)
    }
  }
})

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
