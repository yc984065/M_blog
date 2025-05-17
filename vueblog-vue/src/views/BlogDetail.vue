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

      <el-divider content-position="left"><h3><i class="el-icon-chat-dot-round"></i> 评论区 ({{ comments.length }})</h3></el-divider>
      <div class="comment-section">
        <div v-if="isLoggedIn" class="comment-form">
          <el-avatar :size="40" :src="currentUserAvatar" class="comment-form-avatar"></el-avatar>
          <div class="comment-form-main">
            <el-input
              type="textarea"
              :rows="3"
              placeholder="请输入友善的评论内容..."
              v-model="newCommentText"
              maxlength="500"
              show-word-limit
              class="comment-input"
              ref="commentInput"
            >
            </el-input>
            <el-button
              type="primary"
              size="small"
              @click="submitComment"
              :loading="commentSubmitLoading"
              class="comment-submit-button"
            >
              发表评论
            </el-button>
          </div>
        </div>
        <div v-else class="comment-login-prompt">
          请先<el-link type="primary" @click="$router.push('/login')" style="margin: 0 5px;">登录</el-link>后参与评论
        </div>

        <div v-if="commentsLoading" class="loading-placeholder" style="min-height: 100px; padding: 20px;">
          <i class="el-icon-loading"></i> 评论加载中...
        </div>
        <ul v-else-if="comments && comments.length > 0" class="comment-list">
          <li v-for="(comment, index) in comments" :key="comment.id" class="comment-item">
            <el-avatar :size="40" :src="comment.userAvatar || defaultAvatarUrl" class="comment-avatar"></el-avatar>
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-username">{{ comment.username || '匿名用户' }}</span>
                <el-tag size="mini" type="warning" effect="dark" v-if="comment.userRole === 'admin'" class="admin-tag">
                  管理员
                </el-tag>
                <span class="comment-time">{{ formatDateTime(comment.createdAt) }}</span>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
              <div class="comment-actions" v-if="canDeleteComment(comment)">
                <el-button
                  type="text"
                  size="mini"
                  @click="confirmDeleteComment(comment.id, index)"
                  :loading="comment.deleteLoading"
                  class="delete-comment-btn"
                  icon="el-icon-delete"
                >
                  删除
                </el-button>
              </div>
            </div>
          </li>
        </ul>
        <div v-else class="no-comments">
          暂无评论，快来抢沙发吧！
        </div>
      </div>
    </div>

    <div v-if="loading && !blog.id" class="loading-placeholder"> <i class="el-icon-loading"></i> 博客内容加载中...
    </div>
    <div v-if="!loading && !blog.id" class="not-found-placeholder">
      <p>未能加载博客内容，或该博客不存在。</p>
      <el-button @click="goBack" type="primary">返回列表</el-button>
    </div>
  </div>
</template>

<script>
import Header from "../components/Header";
import { mapGetters } from 'vuex';
import MarkdownIt from "markdown-it";

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
      comments: [],
      newCommentText: "",
      commentsLoading: false,
      commentSubmitLoading: false,
      defaultAvatarUrl: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
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
    canDelete() { // 删除博客的权限
      return this.isOwner || this.isAdmin;
    },
    isLoggedIn() {
      return !!(this.getUser && this.getUser.id);
    },
    currentUserAvatar() {
      if (this.getUser && this.getUser.avatar) {
        return this.getUser.avatar.startsWith('/uploads/') ?
               `http://localhost:8081${this.getUser.avatar}` :
               this.getUser.avatar;
      }
      return this.defaultAvatarUrl;
    }
  },
  created() {
    this.fetchBlogDetail();
  },
  watch: {
    '$route.params.blogId': function(newId, oldId) {
      if (newId && newId !== oldId) {
        this.fetchBlogDetail();
        this.resetAndFetchComments();
      }
    },
    'blog.id': function(newBlogId) {
        if (newBlogId && !this.commentsLoading) {
            if(this.comments.length === 0 || (this.comments.length > 0 && this.comments[0].blogId !== newBlogId)) {
                 this.fetchComments();
            }
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
            const md = new MarkdownIt({ html: true, breaks: true, linkify: true });
            const backendBaseUrl = "http://localhost:8081";
            const defaultImageRenderer = md.renderer.rules.image;
            md.renderer.rules.image = (tokens, idx, options, env, self) => {
              const token = tokens[idx];
              const srcIndex = token.attrIndex('src');
              let srcValue = token.attrs[srcIndex][1];
              if (srcValue && srcValue.startsWith('/uploads/')) {
                token.attrs[srcIndex][1] = backendBaseUrl + srcValue;
              }
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
      }).catch(() => {});
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
    },
    resetAndFetchComments() {
        this.comments = [];
        this.newCommentText = "";
        if (this.blog.id) {
            this.fetchComments();
        }
    },
    fetchComments() {
      if (!this.blog.id) {
        return;
      }
      this.commentsLoading = true;
      this.$axios.get(`/api/blogs/${this.blog.id}/comments`)
        .then(res => {
          if (res.data.code === 200 && Array.isArray(res.data.data)) {
            this.comments = res.data.data.map(comment => {
              if (comment.userAvatar && comment.userAvatar.startsWith('/uploads/')) {
                comment.userAvatar = `http://localhost:8081${comment.userAvatar}`;
              } else if (!comment.userAvatar) {
                comment.userAvatar = this.defaultAvatarUrl;
              }
              comment.deleteLoading = false;
              return comment;
            });
          } else {
            this.$message.error(res.data.msg || "加载评论失败");
            this.comments = [];
          }
        })
        .catch(error => {
          console.error("加载评论失败:", error.response || error);
          this.$message.error(error.response?.data?.msg || "请求评论列表失败");
          this.comments = [];
        })
        .finally(() => {
          this.commentsLoading = false;
        });
    },
    submitComment() {
      if (!this.newCommentText.trim()) {
        this.$message.warning("评论内容不能为空");
        this.$refs.commentInput.focus();
      }
      if (!this.blog.id) {
        this.$message.error("无法评论：未指定博客ID");
        return;
      }
      this.commentSubmitLoading = true;
      this.$axios.post(`/api/blogs/${this.blog.id}/comments`, { content: this.newCommentText })
        .then(res => {
          if (res.data.code === 200 && res.data.data) {
            this.$message.success("评论发表成功！");
            this.newCommentText = "";
            // 后端返回了新评论对象，直接添加到列表顶部
            const newCommentData = res.data.data;
            if (newCommentData.userAvatar && newCommentData.userAvatar.startsWith('/uploads/')) {
                newCommentData.userAvatar = `http://localhost:8081${newCommentData.userAvatar}`;
            } else if (!newCommentData.userAvatar) {
                newCommentData.userAvatar = this.defaultAvatarUrl;
            }
            newCommentData.deleteLoading = false;
            this.comments.unshift(newCommentData);
          } else {
            this.$message.error(res.data.msg || "评论发表失败");
          }
        })
        .catch(error => {
          console.error("发表评论失败:", error.response || error);
          this.$message.error(error.response?.data?.msg || "请求发表评论失败");
        })
        .finally(() => {
          this.commentSubmitLoading = false;
        });
    },
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return '';
      try {
        const date = new Date(dateTimeStr);
        if (isNaN(date.getTime())) return '无效日期';
        return date.toLocaleString('zh-CN', {
          year: 'numeric', month: '2-digit', day: '2-digit',
          hour: '2-digit', minute: '2-digit'
        });
      } catch(e) {
        return dateTimeStr;
      }
    },
    canDeleteComment(comment) {
      if (!this.isLoggedIn) return false; // 未登录不能删除
      // 管理员可以删除任何评论，或者评论的作者可以删除自己的评论
      return this.isAdmin || (this.getUser && this.getUser.id === comment.userId);
    },
    confirmDeleteComment(commentId, index) {
      const commentToDelete = this.comments[index];
      this.$confirm(`确定要删除这条评论吗?`, '提示', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(() => {
        this.deleteCommentById(commentId, index);
      }).catch(() => {
        // this.$message.info('已取消删除');
      });
    },
    deleteCommentById(commentId, index) {
      if (!commentId) return;

      // 设置特定评论的加载状态
      if (this.comments[index]) {
        this.$set(this.comments[index], 'deleteLoading', true);
      }

      this.$axios.delete(`/api/comments/${commentId}`)
        .then(res => {
          if (res.data.code === 200) {
            this.$message.success('评论删除成功');
            this.comments.splice(index, 1); // 从列表中移除评论
          } else {
            this.$message.error(res.data.msg || '删除评论失败');
          }
        })
        .catch(err => {
          console.error("删除评论失败:", err.response || err);
          this.$message.error(err.response?.data?.msg || '请求删除评论失败');
        })
        .finally(() => {
          if (this.comments[index]) {
            this.$set(this.comments[index], 'deleteLoading', false);
          }
        });
    }
  },
};
</script>

<style scoped>

.comment-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
.comment-section h3 {
  margin-bottom: 20px;
  font-size: 1.5em;
  color: #303133;
  display: flex;
  align-items: center;
}
.comment-section h3 .el-icon-chat-dot-round {
  margin-right: 8px;
  color: #409EFF;
}

.comment-form {
  display: flex;
  align-items: flex-start;
  margin-bottom: 25px;
}
.comment-form-avatar {
  margin-right: 15px;
  flex-shrink: 0;
}
.comment-form-main {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}
.comment-input {
  margin-bottom: 10px;
}
.comment-submit-button {
  align-self: flex-end;
}

.comment-login-prompt {
  text-align: center;
  padding: 20px;
  color: #909399;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  margin-bottom: 20px;
}
.comment-login-prompt .el-link {
  font-size: inherit;
  vertical-align: baseline;
}

.comment-list {
  list-style-type: none;
  padding: 0;
}
.comment-item {
  display: flex;
  padding: 15px 0;
  border-bottom: 1px solid #f0f2f5;
}
.comment-item:last-child {
  border-bottom: none;
}
.comment-avatar {
  margin-right: 15px;
  flex-shrink: 0;
}
.comment-body {
  flex-grow: 1;
  text-align: left;
}
.comment-header {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
.comment-username {
  font-weight: bold;
  color: #303133;
  margin-right: 8px;
}
.admin-tag {
  margin-right: 8px;
  height: 20px;
  line-height: 18px;
  padding: 0 6px;
  background-color: #E6A23C;
  border-color: #E6A23C;
  color: #fff;
}
.comment-time {
  font-size: 0.8em;
  color: #909399;
}
.comment-content {
  font-size: 0.95em;
  line-height: 1.6;
  color: #606266;
  word-wrap: break-word;
  white-space: pre-wrap;
}
.no-comments {
  text-align: center;
  color: #909399;
  padding: 30px 0;
  font-style: italic;
}
.comment-actions {
  margin-top: 5px;
  text-align: right;
}
.delete-comment-btn {
  padding: 3px 7px;
  color: #F56C6C;
}
.delete-comment-btn:hover {
  color: #f78989;
}

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
