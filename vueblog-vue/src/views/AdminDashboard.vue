<template>
  <div class="admin-dashboard-page"> 
    <Header /> 
    <div class="admin-dashboard-container">
      <h1>管理员控制台</h1>
      
      <p v-if="getUser">欢迎您，管理员 <strong>{{ adminUsername }}</strong>！</p>
      <p v-else>正在加载用户信息...</p>

      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8"> 
          <el-card shadow="hover" class="dashboard-card" @click.native="navigateTo({ name: 'AdminKeywords' })">
            <div class="card-content">
              <i class="el-icon-key card-icon"></i>
              <h3>关键字管理</h3>
              <p>添加、删除和查看内容过滤关键字。</p>
            </div>
          </el-card>
        </el-col>
         <el-col :xs="24" :sm="12" :md="8">
           <el-card shadow="hover" class="dashboard-card" @click.native="navigateTo({ name: 'AdminBlogManagement' })">
             <div class="card-content">
               <i class="el-icon-document-copy card-icon" style="color: #E6A23C;"></i>
               <h3>博客管理</h3> <p>查看、搜索和删除所有用户发布的博客。</p> </div>
           </el-card>
         </el-col>
         </el-row>
    </div>
    <p style="text-align:center; color: #ccc; margin-top: 20px;">-- 管理仪表盘底部 --</p>
  </div>
</template>

<script>
import Header from '@/components/Header.vue'; // 请再次确认 Header 组件的实际路径
import { mapGetters } from 'vuex';

export default {
  name: 'AdminDashboard',
  components: { Header },
  computed: {
    ...mapGetters(['getUser']), 
    adminUsername() {
      // console.log('AdminDashboard computed: getUser is', this.getUser); 
      return this.getUser ? this.getUser.username : '管理员';
    }
  },
  methods: {
    navigateTo(routeTarget) { // routeTarget 可以是路径字符串或路由对象 { name: 'RouteName' }
      let isSameRoute = false;
      const currentRoute = this.$route;

      if (typeof routeTarget === 'string') {
        isSameRoute = currentRoute.path === routeTarget;
      } else if (typeof routeTarget === 'object' && routeTarget !== null) {
        if (routeTarget.name) {
          isSameRoute = currentRoute.name === routeTarget.name && 
                        JSON.stringify(currentRoute.params) === JSON.stringify(routeTarget.params || {}) &&
                        JSON.stringify(currentRoute.query) === JSON.stringify(routeTarget.query || {});
        } else if (routeTarget.path) {
          isSameRoute = currentRoute.path === routeTarget.path &&
                        JSON.stringify(currentRoute.query) === JSON.stringify(routeTarget.query || {});
        }
      }

      if (!isSameRoute) {
        this.$router.push(routeTarget).catch(err => {
          if (err.name !== 'NavigationDuplicated') {
            // 对于非重复导航错误，仍然建议打印出来以便调试
            console.error('Router navigation error:', err);
          }
        });
      } else {
        console.log("Already on the target route or navigating to the same route.");
      }
    }
  },
  created() {
    console.log('AdminDashboard component created.');
  },
  mounted() {
    console.log('AdminDashboard component mounted.');
  }
};
</script>

<style scoped>
.admin-dashboard-page {
  display: flex;
  flex-direction: column;
}
.admin-dashboard-container {
  padding: 20px;
  max-width: 1100px; /* 可以适当调整宽度 */
  margin: 20px auto;
  flex-grow: 1; /* 使容器在 flex 布局中填充空间 */
}
.dashboard-card {
  cursor: pointer;
  text-align: center;
  margin-bottom: 20px;
  transition: all 0.3s ease; /* 添加悬浮效果 */
}
.dashboard-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.1);
}
.card-content {
  padding: 20px;
}
.card-icon {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 15px;
  display: block; /* 确保图标独占一行 */
}
.dashboard-card h3 {
  margin: 10px 0;
  font-size: 1.2em;
  color: #303133;
}
.dashboard-card p {
  font-size: 0.9em;
  color: #606266;
  line-height: 1.5;
}
h1 {
  color: #303133;
  margin-bottom: 10px;
}
p { /* 针对 <p v-if="getUser"> 的样式 */
  color: #606266;
  margin-bottom: 20px;
}
</style>
