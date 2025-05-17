<template>
  <div>
    <el-container>
      <el-header>
        <img class="mlogo" src="https://www.markerhub.com/dist/images/logo/markerhub-logo.png" alt="">
      </el-header>
      <el-main>
        <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="ruleForm.username"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input type="password" v-model="ruleForm.password"></el-input>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="ruleForm.email"></el-input>
          </el-form-item>
          
          <el-form-item label="注册身份" prop="role">
            <el-select v-model="ruleForm.role" placeholder="请选择身份">
              <el-option label="普通用户" value="user"></el-option>
              <el-option label="管理员" value="admin"></el-option>
              </el-select>
          </el-form-item>

          <el-form-item label="头像" prop="avatar"> <input type="file" @change="handleFileUpload">
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="submitForm('ruleForm')">注册</el-button>
            <el-button @click="resetForm('ruleForm')">重置</el-button>
          </el-form-item>
          
          <el-form-item>
            <el-link type="primary" href="/login">已有账号？立即登录</el-link>
          </el-form-item>
          <el-form-item>
            <el-button @click="goHome">返回主页</el-button>
          </el-form-item>
        </el-form>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: "Register",
  data() {
    return {
      ruleForm: {
        username: '',
        password: '',
        email: '',
        role: 'user', // 默认角色为 'user'
        avatar: null // 用于存储选择的文件对象
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 15, message: '长度在 3 到 15 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] } 
        ],
        role: [ // 为角色添加校验规则
          { required: true, message: '请选择注册身份', trigger: 'change' }
        ]
      }
    };
  },
  methods: {
    handleFileUpload(event) {
      this.ruleForm.avatar = event.target.files[0];
    },
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          const formData = new FormData();
          formData.append("username", this.ruleForm.username);
          formData.append("password", this.ruleForm.password);
          formData.append("email", this.ruleForm.email);
          formData.append("role", this.ruleForm.role);

          if (this.ruleForm.avatar) {
            formData.append("avatar", this.ruleForm.avatar);
          }
          
          this.$axios.post('/register', formData, {
            headers: {
              "Content-Type": "multipart/form-data"
            }
          }).then(res => {
            if (res.data.code === 200) { // 假设后端成功时 code 为 200
                 this.$alert('注册成功', '提示', {
                    confirmButtonText: '确定',
                    callback: action => {
                        this.$router.push("/login")
                    }
                });
            } else {
                // 如果后端返回 code !== 200 但 HTTP 状态码是 2xx，错误通常在 res.data.msg 中
                this.$message.error(res.data.msg || '注册失败');
            }
          }).catch(err => {
            // Axios 的 catch 通常处理网络错误或 HTTP 状态码为 4xx/5xx 的情况
            const errorMsg = err.response?.data?.msg || err.message || '注册请求失败';
            this.$message.error(errorMsg);
          });
        } else {
          console.log('error submit!!');
          return false;
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
      this.ruleForm.avatar = null;
      this.ruleForm.role = 'user'; 
      const fileInput = this.$el.querySelector('input[type="file"]');
      if (fileInput) {
        fileInput.value = '';
      }
    },
    goHome() {
      this.$router.push("/");
    }
  }
}
</script>

<style scoped>
.mlogo {
  height: 60%;
  margin-top: 10px;
}
.demo-ruleForm {
  max-width: 500px;
  margin: 0 auto;
}
</style>