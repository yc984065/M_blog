<template>
  <div>
    <Header></Header>

    <div class="m-content">
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
        <el-form-item label="标题" prop="title">
          <el-input v-model="ruleForm.title"></el-input>
        </el-form-item>

        <el-form-item label="摘要" prop="description">
          <el-input type="textarea" v-model="ruleForm.description"></el-input>
        </el-form-item>

        <!-- 新增分类选择 -->
        <el-form-item label="分类" prop="categories">
          <el-select 
            v-model="ruleForm.categoryIds" 
            multiple 
            placeholder="请选择分类"
            style="width: 100%"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <!-- 新增标签选择 -->
        <el-form-item label="标签" prop="tags">
          <el-select
            v-model="ruleForm.tagIds"
            multiple
            filterable
            allow-create
            placeholder="请输入或选择标签"
            style="width: 100%"
            @change="handleTagChange"
          >
            <el-option
              v-for="tag in tags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            >
              <span :style="{color: tag.color}">{{ tag.name }}</span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <mavon-editor 
    v-model="ruleForm.content" 
    ref="mdEditor"
    :toolbars="toolbars"
    @imgAdd="handleImageAdd"
  ></mavon-editor>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm('ruleForm')">发布</el-button>
          <el-button @click="resetForm('ruleForm')">重置</el-button>
          <el-button 
            type="success" 
            @click="getGemmaSuggestions"
            :loading="loading"
            :disabled="!ruleForm.content"
          >
            <i class="el-icon-lightbulb"></i> 获取AI建议 (Gemma 3)
          </el-button>
        </el-form-item>
      </el-form>

      <el-dialog 
        title="Gemma 3 写作建议" 
        :visible.sync="dialogVisible" 
        width="70%"
        top="5vh"
        :close-on-click-modal="false"
      >
        <div v-if="loading" class="loading-spinner">
          <i class="el-icon-loading"></i> 正在生成建议，请稍候...
        </div>
        <div v-else>
          <mavon-editor
            v-model="suggestions"
            :subfield="false"
            defaultOpen="preview"
            :toolbarsFlag="false"
            :editable="false"
            style="min-height: 300px"
          />
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="applySuggestion">应用建议</el-button>
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </el-dialog>

      <!-- 新增分类选择 -->
      <el-form-item label="分类" prop="categories">
          <el-select 
            v-model="ruleForm.categoryIds" 
            multiple 
            placeholder="请选择分类"
            style="width: 100%"
            @change="handleCategoryChange"  
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <!-- 新增标签选择 -->
        <el-form-item label="标签" prop="tags">
          <el-select
            v-model="ruleForm.tagIds"
            multiple
            filterable
            allow-create
            placeholder="请输入或选择标签"
            style="width: 100%"
            @change="handleTagChange"
          >
            <el-option
              v-for="tag in tags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            >
              <span :style="{color: tag.color}">{{ tag.name }}</span>
            </el-option>
          </el-select>
        </el-form-item>
    </div>
  </div>
</template>

<script>
import Header from "../components/Header";
import axios from "../axios";

export default {
  name: "BlogEdit",
  components: { Header },
  data() {
    return {
      ruleForm: {
        id: "",
        title: "",
        description: "",
        content: "",
        categoryIds: [],
        tagIds: []
      },
      categories: [],
      tags: [],
      rules: {
        title: [
          { required: true, message: "请输入标题", trigger: "blur" },
          { min: 3, max: 25, message: "长度在 3 到 25 个字符", trigger: "blur" },
        ],
        description: [{ required: true, message: "请输入摘要", trigger: "blur" }],
        content: [{ required: true, message: "请输入内容", trigger: "blur" }],
      },
      suggestions: "",
      loading: false,
      dialogVisible: false,
    };
  },
  methods: {
    handleCategoryChange(selectedCategories) {
      // 分类变化时更新标签框
      if (selectedCategories && selectedCategories.length > 0) {
        // 获取选择的第一个分类的标签
        axios.get(`/categories/${selectedCategories[0]}/tags`).then((res) => {
          this.selectedCategoryTags = res.data.data;  // 更新当前分类的标签
          this.tags = this.selectedCategoryTags;      // 更新标签选择框的数据
          
          // 如果该博客已经有标签，确保这些标签也会出现在标签框中
          if (this.ruleForm.tagIds && this.ruleForm.tagIds.length > 0) {
            this.ruleForm.tagIds = this.ruleForm.tagIds.filter(tagId => 
              this.selectedCategoryTags.some(tag => tag.id === tagId)
            );
          }
        }).catch((error) => {
          this.$message.error("加载标签失败: " + error.message);
        });
      }
    },
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          const _this = this;
          axios.post("/blog/edit", this.ruleForm, {
            headers: {
              Authorization: localStorage.getItem("token"),
            },
          }).then((res) => {
            // 提交分类和标签
            if (this.ruleForm.id) {
              this.updateAssociations();
            }
            this.$alert("操作成功", "提示", {
              confirmButtonText: "确定",
              callback: () => {
                this.$router.push("/blogs");
              },
            });
          }).catch(error => {
            this.$message.error("提交失败: " + error.message);
          });
        }
      });
    },

    updateAssociations() {
      const blogId = this.ruleForm.id;
      if (this.ruleForm.categoryIds && this.ruleForm.categoryIds.length > 0) {
        axios.post(`/blog/${blogId}/categories`, this.ruleForm.categoryIds);
      }
      if (this.ruleForm.tagIds && this.ruleForm.tagIds.length > 0) {
        axios.post(`/blog/${blogId}/tags`, this.ruleForm.tagIds);
      }
    },

    handleTagChange(tags) {
      // 处理新创建的标签
      const newTags = tags.filter(t => typeof t === 'string');
      if (newTags.length > 0) {
        newTags.forEach(async tagName => {
          try {
            const res = await axios.post('/tags', { name: tagName });
            this.tags.push(res.data.data);
            this.ruleForm.tagIds = this.ruleForm.tagIds.map(t => 
              t === tagName ? res.data.data.id : t
            );
          } catch (error) {
            console.error("创建标签失败:", error);
          }
        });
      }
    },
    
    resetForm(formName) {
      this.$refs[formName].resetFields();
      this.ruleForm.categoryIds = [];
      this.ruleForm.tagIds = [];
    },
    
    async getGemmaSuggestions() {
      if (!this.ruleForm.content.trim()) {
        this.$message.warning("请先输入内容");
        return;
      }

      this.loading = true;
      this.dialogVisible = true;
      this.suggestions = "正在生成建议...";

      try {
        const response = await this.$axios.post('/api/ai/ask', {
          prompt: `${this.ruleForm.content}`
        });
        this.suggestions = response.data.data || "未获取到建议内容";
      } catch (error) {
        let errorMsg = error.response?.data?.msg || error.message;
        if (errorMsg.includes("timed out")) {
          errorMsg = "请求超时，请检查网络连接";
        }
        this.suggestions = `获取建议失败: ${errorMsg}`;
      } finally {
        this.loading = false;
      }
    },
    
    applySuggestion() {
      if (this.suggestions && !this.suggestions.startsWith("获取建议失败")) {
        this.ruleForm.content += `\n\n---\n\n${this.suggestions}`;
        this.$message.success("建议已添加到编辑区");
      }
      this.dialogVisible = false;
    },

    fetchCategoriesAndTags() {
      axios.get('/categories').then(res => {
        this.categories = res.data.data;
      });
      axios.get('/tags').then(res => {
        this.tags = res.data.data;
      });
    }
  },
  
  created() {
    this.fetchCategoriesAndTags();
    const blogId = this.$route.params.blogId;
    if (blogId) {
      axios.get("/blog/" + blogId).then((res) => {
        const blog = res.data.data;
        this.ruleForm = {
          id: blog.id,
          title: blog.title,
          description: blog.description,
          content: blog.content,
          categoryIds: blog.categories ? blog.categories.map(c => c.id) : [],
          tagIds: blog.tags ? blog.tags.map(t => t.id) : []
        };
      }).catch(error => {
        this.$message.error("加载博客失败: " + error.message);
      });
    }
  }
};
</script>

<style scoped>
.m-content {
  text-align: center;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.loading-spinner {
  text-align: center;
  padding: 50px;
  font-size: 16px;
  color: #666;
}

.el-dialog__body {
  padding: 20px;
}

.el-tag + .el-tag {
  margin-left: 10px;
}

.markdown-body img {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 10px auto;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
</style>

