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
          <el-form-item label="头像" prop="avatar">
            <input type="file" @change="handleFileUpload">
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="submitForm('ruleForm')">注册</el-button>
            <el-button @click="resetForm('ruleForm')">重置</el-button>
          </el-form-item>
          
          <!-- 添加的导航链接 -->
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
        avatar: null
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
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
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
          if (this.ruleForm.avatar) {
            formData.append("avatar", this.ruleForm.avatar);
          }

          this.$axios.post('/register', formData, {
            headers: {
              "Content-Type": "multipart/form-data"
            }
          }).then(res => {
            this.$alert('注册成功', '提示', {
              confirmButtonText: '确定',
              callback: action => {
                this.$router.push("/login")
              }
            });
          }).catch(err => {
            const errorMsg = err.response.data.msg || '注册失败';
            if (errorMsg.includes('用户名')) {
              this.$alert('用户名已存在', '错误', {
                confirmButtonText: '确定',
                callback: action => {
                  // 保留已填写的信息，不需要做任何操作，因为表单数据未被清除
                }
              });
            } else {
              this.$message.error(errorMsg);
            }
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