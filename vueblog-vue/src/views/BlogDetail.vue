<template>
  <div>
    <Header></Header>

    <div class="mblog" v-if="!loading && blog.id">
      <h1>{{ blog.title }}</h1>
      <h2 class="blog-description">{{ blog.description }}</h2>
      
      <div class="actions-bar">
        <el-link icon="el-icon-edit" v-if="canEdit" class="action-link">
          <router-link :to="{ name: 'BlogEdit', params: { blogId: blog.id } }">
            编辑
          </router-link>
        </el-link>
        <el-link icon="el-icon-delete" type="danger" v-if="canDelete" @click="confirmDeleteBlog" class="action-link">
          删除
        </el-link>
      </div>
      
      <div class="meta-info">
        <div class="category-box" v-if="blog.categories && blog.categories.length > 0">
          <span class="meta-title">📂 分类：</span>
          <el-tag 
            v-for="category in blog.categories"
            :key="'c-' + category.id"
            type="success"
            class="meta-tag"
          >
            {{ category.name }}
          </el-tag>
        </div>
        <div v-else class="category-box">
          <span class="meta-title">📂 分类：</span>
          <el-tag type="info" class="meta-tag">暂无分类</el-tag>
        </div>

        <div class="tag-box" v-if="blog.tags && blog.tags.length > 0">
          <span class="meta-title">🏷️ 标签：</span>
          <el-tag 
            v-for="tag in blog.tags"
            :key="'t-' + tag.id"
            :color="tag.color || '#409EFF'" 
            class="meta-tag tag"
            effect="dark"
          >
            {{ tag.name }}
          </el-tag>
        </div>
         <div v-else class="tag-box">
          <span class="meta-title">🏷️ 标签：</span>
          <el-tag type="info" class="meta-tag">暂无标签</el-tag>
        </div>
      </div>
      
      <el-divider></el-divider>
      <div class="markdown-body" v-html="blog.content"></div>
    </div>
    <div v-if="loading" class="loading-placeholder">
      <i class="el-icon-loading"></i> 博客内容加载中...
    </div>
    <div v-if="!loading && !blog.id" class="not-found-placeholder">
      <p>未能加载博客内容，或该博客不存在。</p>
      <el-button @click="goBack" type="primary">返回列表</el-button>
    </div>
  </div>
</template>

<script>
// import "github-markdown-css"; // 用户已移除
import Header from "../components/Header"; 
import { mapGetters } from 'vuex';     
import MarkdownIt from "markdown-it"; // 显式导入 MarkdownIt

export default {
  name: "BlogDetail",
  components: { Header },
  data() {
    return {
      blog: { 
        id: null,
        title: "加载中...",
        description: "",
        content: "",
        userId: null, 
        categories: [],
        tags: []
      },
      loading: true, 
    };
  },
  computed: {
    ...mapGetters(['getUser', 'isAdmin']), 
    isOwner() {
      return this.getUser && this.blog && this.blog.userId === this.getUser.id;
    },
    canEdit() {
      return this.isOwner;
    },
    canDelete() {
      return this.isOwner || this.isAdmin;
    }
  },
  created() {
    this.fetchBlogDetail(); 
  },
  watch: {
    '$route.params.blogId': function(newId, oldId) {
      if (newId && newId !== oldId) {
        this.fetchBlogDetail();
      }
    }
  },
  methods: {
    fetchBlogDetail() {
      this.loading = true; 
      const blogId = this.$route.params.blogId; 

      if (!blogId) { 
        this.$message.error("无效的博客ID");
        this.loading = false;
        this.$router.push({ name: 'UserPage' }); 
        return;
      }

      this.$axios.get("/blog/" + blogId).then((res) => { 
        if (res.data.code === 200 && res.data.data) {
          const blogData = res.data.data;
          this.blog.id = blogData.id;
          this.blog.title = blogData.title || "无标题";
          this.blog.description = blogData.description || "";
          this.blog.userId = blogData.userId; 
          this.blog.categories = blogData.categories || [];
          this.blog.tags = blogData.tags || [];

          if (blogData.content) { 
            // 初始化 MarkdownIt
            const md = new MarkdownIt({ 
              html: true,    // 允许 HTML 标签
              breaks: true,  // 将 \n 转换成 <br>
              linkify: true  // 自动识别链接
            });

            // **核心修改：覆盖默认的图片渲染规则**
            const backendBaseUrl = "http://localhost:8081"; // 【重要】您的后端服务器地址和端口
            const defaultImageRenderer = md.renderer.rules.image;
            md.renderer.rules.image = (tokens, idx, options, env, self) => {
              const token = tokens[idx];
              const srcIndex = token.attrIndex('src');
              let srcValue = token.attrs[srcIndex][1]; // 获取原始 src 值

              // 如果 src 是以 /uploads/ 开头的相对路径，则添加后端服务器前缀
              if (srcValue && srcValue.startsWith('/uploads/')) {
                token.attrs[srcIndex][1] = backendBaseUrl + srcValue;
              }
              // 调用原始的渲染规则来渲染图片标签
              return defaultImageRenderer(tokens, idx, options, env, self);
            };
            
            this.blog.content = md.render(blogData.content);
          } else {
            this.blog.content = "<p style='text-align:center; color:#999;'>暂无内容</p>";
          }
        } else {
          this.$message.error(res.data.msg || "加载博客详情失败");
          this.blog.id = null; 
        }
      }).catch((error) => { 
        console.error("加载博客详情请求失败:", error.response || error);
        this.$message.error(error.response?.data?.msg || "加载博客详情时发生网络错误");
        this.blog.id = null; 
      }).finally(() => {
        this.loading = false; 
      });
    },
    confirmDeleteBlog() {
      this.$confirm(`确定要永久删除博客《${this.blog.title}》吗? 此操作不可恢复。`, '警告：删除博客', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        center: true 
      }).then(() => {
        this.deleteBlog(); 
      }).catch(() => {
        // this.$message.info('已取消删除操作'); 
      });
    },
    deleteBlog() {
      if (!this.blog.id) return; 

      this.$axios.delete("/blog/" + this.blog.id)
        .then((res) => {
          if (res.data.code === 200) {
            this.$message.success("博客删除成功！");
            if (this.isAdmin) { 
              this.$router.push({ name: 'AdminBlogManagement' }); 
            } else { 
              this.$router.push({ name: 'UserPage' }); 
            }
          } else {
            this.$message.error(res.data.msg || "删除失败");
          }
        })
        .catch((error) => { 
          console.error("删除博客请求失败:", error.response || error);
          this.$message.error(error.response?.data?.msg || "删除博客时发生网络错误");
        });
    },
    goBack() {
      if (this.isAdmin) { 
         this.$router.push({ name: 'AdminBlogManagement' });
      } else {
         this.$router.push({ name: 'UserPage' });
      }
    }
  },
};
</script>

<style scoped>
.mblog {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  width: 100%;
  min-height: 600px; 
  padding: 30px 25px; 
  background-color: #fff;
  border-radius: 8px;
  margin-top: 20px; 
}

.mblog h1 {
  font-size: 2.2em; 
  margin-bottom: 15px; 
  color: #303133;
  font-weight: 600; 
  line-height: 1.3;
}

.blog-description { 
  font-size: 1.1em;
  color: #555; 
  margin-bottom: 25px; 
  font-style: italic;
  line-height: 1.6;
}

.actions-bar {
  margin-bottom: 25px; 
  text-align: right; 
}
.action-link {
  margin-left: 18px; 
  font-size: 0.95em; 
}
.action-link .router-link, 
.action-link a { 
  text-decoration: none;
  color: inherit; 
}
.action-link:hover {
  opacity: 0.8; 
}

.markdown-body {
  text-align: left;
  padding: 20px 0; 
  line-height: 1.75; 
  font-size: 16px; 
  color: #333;
}

.meta-info {
  margin: 25px 0; 
  padding: 18px; 
  background-color: #f9fafc; 
  border: 1px solid #e9eef3; 
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  gap: 15px; 
}

.category-box,
.tag-box {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.meta-title {
  margin-right: 10px; 
  font-weight: 500; 
  color: #454545;
  font-size: 0.9em; 
}

.meta-tag {
  margin: 3px 5px 3px 0; 
  font-size: 0.8em; 
  padding: 0 8px; 
  height: 24px; 
  line-height: 22px; 
}

.loading-placeholder, .not-found-placeholder {
  text-align: center;
  padding: 60px 20px; 
  color: #909399;
  font-size: 1.1em;
  min-height: 300px; 
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
.loading-placeholder i {
  font-size: 2.5em; 
  display: block;
  margin-bottom: 15px;
}
.not-found-placeholder p {
  margin-bottom: 20px;
}
</style>
�