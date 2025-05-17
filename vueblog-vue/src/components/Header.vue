<template>
  <div class="m-content">
    <h3>欢迎来到Mrblue的博客</h3>
    <div class="block">
      <el-avatar :size="50" :src="avatarUrl"></el-avatar>
      <div>{{ user.username }}</div>
    </div>

    <div class="maction">
      <span>
        <el-link @click="navigateTo(userHomePageLink)" :underline="false">主页</el-link>
      </span>
      <el-divider direction="vertical"></el-divider>
      <span>
        <el-link type="success" @click="navigateTo('/blog/add')" :underline="false">发表博客</el-link>
      </span>

      <el-divider direction="vertical"></el-divider>
      <span v-if="!hasLogin">
        <el-link type="primary" @click="navigateTo('/login')" :underline="false">登录</el-link>
      </span>

      <span v-if="hasLogin">
        <el-link type="danger" @click="logout" :underline="false">退出</el-link>
      </span>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'; 

export default {
  name: "Header",
  computed: {
    ...mapGetters(['getUser', 'isAdmin']),
    
    user() {
      return this.getUser || { username: '请先登录', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png' };
    },
    hasLogin() {
      return !!this.$store.state.token && this.getUser && typeof this.getUser.id !== 'undefined';
    },
    avatarUrl() {
      const avatarPath = this.user.avatar;
      if (avatarPath && avatarPath.startsWith('/uploads/')) {
        return `http://localhost:8081${avatarPath}`; // 确保端口和后端一致
      }
      return avatarPath || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
    },
    userHomePageLink() {
      if (this.hasLogin && this.isAdmin) {
        return { name: 'AdminDashboard' }; // 或者 { name: 'AdminDashboard' }
      }
      return { name: 'UserPage' }; // 或者 { name: 'UserPage' }
    }
  },
  methods: {
    logout() {
      this.$axios.get("/logout").then(res => {
        this.$store.commit("REMOVE_INFO");
        this.$router.push("/login"); // 跳转到登录页
      }).catch(error => {
        console.error("退出操作异常:", error);
        this.$store.commit("REMOVE_INFO");
        this.$router.push("/login");
      });
    },
    goToAdminDashboard() {
      this.$router.push({ name: 'AdminDashboard' });
    },
    navigateTo(path) { // 通用导航方法
      if (this.$route.path !== path && !(this.$route.name === path.name && JSON.stringify(this.$route.params) === JSON.stringify(path.params))) {
        // 如果是字符串路径
        if (typeof path === 'string') {
          this.$router.push(path).catch(err => {
            if (err.name !== 'NavigationDuplicated') {
              console.error(err);
            }
          });
        } 
        // 如果是对象路径 (例如 { name: 'RouteName' })
        else if (typeof path === 'object' && path !== null) {
           this.$router.push(path).catch(err => {
            if (err.name !== 'NavigationDuplicated') {
              console.error(err);
            }
          });
        }
      }
    }
  }
}
</script>

<style scoped>
.m-content {
  max-width: 960px;
  margin: 0 auto;
  text-align: center;
}
.maction {
  margin: 10px 0;
}
.el-link { /* 确保链接有指针手势 */
  cursor: pointer;
}
</style>
