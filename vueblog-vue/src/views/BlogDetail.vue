<template>
  <div>
    <Header></Header>

    <div class="mblog">
      <h1>{{ blog.title }}</h1>
      <h2>{{ blog.description }}</h2>
      <el-link icon="el-icon-edit" v-if="ownBlog">
        <router-link :to="{ name: 'BlogEdit', params: { blogId: blog.id } }">
          编辑
        </router-link>
      </el-link>
      <el-link icon="el-icon-delete" type="danger" v-if="ownBlog && blog.id" @click="deleteBlog">删除</el-link>
      
      <!-- 分类与标签展示 -->
<div class="meta-info">
  <div class="category-box">
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

  <div class="tag-box">
    <span class="meta-title">🏷️ 标签：</span>
    <el-tag 
      v-for="tag in blog.tags"
      :key="'t-' + tag.id"
      :color="tag.color"
      class="meta-tag tag"
      effect="dark"
    >
      {{ tag.name }}
    </el-tag>
  </div>
</div>

      
      <el-divider></el-divider>
      <div class="markdown-body" v-html="blog.content"></div>
    </div>
  </div>
</template>

<script>
import "github-markdown-css";
import Header from "../components/Header";

export default {
  name: "BlogDetail.vue",
  components: { Header },
  data() {
    return {
      blog: {
        id: "",
        title: "",
        description: "",
        content: "",
        categories: [],
        tags: []
        
      },
      ownBlog: false,
      loading: true,
    };
  },
  created() {
    const blogId = this.$route.params.blogId;
    const _this = this;
    this.$axios.get("/blog/" + blogId).then((res) => {
      const blog = res.data.data;
      _this.blog.id = blog.id;
      _this.blog.title = blog.title;
      _this.blog.description = blog.description;
      _this.blog.categories = blog.categories || [];
      _this.blog.tags = blog.tags || [];

      var MardownIt = require("markdown-it");
      var md = new MardownIt();
      var result = md.render(blog.content);
      _this.blog.content = result;
      _this.ownBlog = blog.userId === _this.$store.getters.getUser.id;
      _this.loading = false;
    }).catch(() => {
      _this.loading = false;
    });
  },
  methods: {
    deleteBlog() {
      const _this = this;
      this.$axios
        .delete("/blog/" + this.blog.id, {
          headers: {
            Authorization: localStorage.getItem("token"),
          },
        })
        .then((res) => {
          if (res.data.code === 200) {
            _this.$message.success("删除成功");
            _this.$router.push("/blogs");
          } else {
            _this.$message.error("删除失败");
          }
        })
        .catch((error) => {
          console.error("删除博客失败:", error);
          _this.$message.error("删除失败");
        });
    },
  },
};
</script>

<style scoped>
.mblog {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  width: 100%;
  min-height: 700px;
  padding: 20px 15px;
}



.markdown-body {
  text-align: left;
  padding: 20px;
}

.meta-info {
  margin: 20px 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.category-box,
.tag-box {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.meta-title {
  margin-right: 10px;
  font-weight: 600;
  color: #666;
}

.meta-tag {
  margin: 5px 8px 5px 0;
  font-size: 13px;
}

.tag {
  color: white;
}

</style>