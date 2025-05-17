<template>
  <div>
    <Header></Header>

    <div class="m-content">
      <div v-loading="pageLoading" 
           element-loading-text="正在加载博客数据，请稍候..."
           element-loading-spinner="el-icon-loading"
           element-loading-background="rgba(255, 255, 255, 0.7)"
           class="form-loading-container">
        <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
          <el-form-item label="标题" prop="title">
            <el-input v-model="ruleForm.title" placeholder="请输入博客标题"></el-input>
          </el-form-item>

          <el-form-item label="摘要" prop="description">
            <el-input type="textarea" v-model="ruleForm.description" placeholder="请输入博客摘要" rows="3"></el-input>
          </el-form-item>

          <el-form-item label="分类" prop="categoryIds">
            <el-select 
              v-model="ruleForm.categoryIds" 
              multiple 
              filterable
              placeholder="请选择或搜索分类"
              style="width: 100%"
              @change="handleCategoryChange"
              :loading="pageLoading" 
            >
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="标签" prop="tagIds">
            <el-select
              v-model="ruleForm.tagIds"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="请输入或选择标签，可创建新标签"
              style="width: 100%"
              @change="handleTagChange"
              :loading="pageLoading"
            >
              <el-option
                v-for="tag in tags"
                :key="tag.id"
                :label="tag.name"
                :value="tag.id"
              >
                <span :style="{color: tag.color || '#409EFF'}">{{ tag.name }}</span>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="内容" prop="content">
            <div v-if="pageLoading && ruleForm.id" style="min-height: 500px; display:flex; align-items:center; justify-content:center; border:1px solid #eee; color:#999;">内容加载中...</div>
            <mavon-editor 
              v-else
              v-model="ruleForm.content" 
              ref="mdEditor"
              :toolbars="toolbars"
              @imgAdd="handleImageAdd"
              placeholder="开始编写你的博客内容..."
              style="min-height: 500px;"
            ></mavon-editor>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="submitForm('ruleForm')" :loading="submitLoading">
              {{ ruleForm.id ? '更新博客' : '发布博客' }}
            </el-button>
            <el-button @click="resetForm('ruleForm')">重置内容</el-button>
            <el-button 
              type="success" 
              @click="getGemmaSuggestions"
              :loading="aiLoading"
              :disabled="pageLoading || !ruleForm.content || !ruleForm.content.trim()" 
              icon="el-icon-lightbulb"
            >
              获取AI建议
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-dialog 
        title="AI 写作建议 (Gemma 3)" 
        :visible.sync="dialogVisible" 
        width="75%" 
        top="5vh"
        :close-on-click-modal="false"
        append-to-body
      >
        <div v-if="aiLoading" class="loading-spinner">
          <i class="el-icon-loading"></i> 正在卖力生成建议，请稍候...
        </div>
        <div v-else class="ai-suggestions-content">
          <mavon-editor
            v-model="suggestions"
            :subfield="false"        
            defaultOpen="preview"   
            :toolbarsFlag="false"   
            :editable="false"       
            boxShadowStyle="none"   
            previewBackground="#f8f9fa" 
            style="min-height: 45vh; border: 1px solid #dcdfe6; border-radius: 4px;"
          />
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="applySuggestion" :disabled="!suggestions || suggestions.startsWith('获取建议失败') || aiLoading">应用建议到正文</el-button>
        </span>
      </el-dialog>
      
    </div>
  </div>
</template>

<script>
import Header from "../components/Header";
import { mapGetters } from 'vuex';

export default {
  name: "BlogEdit",
  components: { Header },
  data() {
    return {
      pageLoading: false, // 页面初始数据加载状态
      ruleForm: {
        id: null,
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
          { min: 3, max: 100, message: "长度在 3 到 100 个字符", trigger: "blur" },
        ],
        description: [
          { required: true, message: "请输入摘要", trigger: "blur" },
          { max: 255, message: "摘要长度不能超过 255 个字符", trigger: "blur" }
        ],
        content: [{ required: true, message: "请输入内容", trigger: "blur" }],
      },
      suggestions: "",
      aiLoading: false, 
      submitLoading: false, 
      dialogVisible: false,
      toolbars: { 
        bold: true, italic: true, header: true, underline: true, strikethrough: true, 
        mark: true, superscript: true, subscript: true, quote: true, ol: true, ul: true,
        link: true, imagelink: true, code: true, table: true, fullscreen: true, 
        readmodel: true, htmlcode: true, help: true, undo: true, redo: true, trash: true,
        save: false, navigation: true, alignleft: true, aligncenter: true, alignright: true,
        subfield: true, preview: true, 
      }
    };
  },
  computed: {
    ...mapGetters(['getUser', 'isAdmin'])
  },
  methods: {
    handleImageAdd(pos, $file) {
        const formData = new FormData();
        formData.append('image', $file);
        this.$axios.post('/api/upload/image', formData, { 
            headers: { 'Content-Type': 'multipart/form-data' }
        }).then(res => {
            if (res.data.code === 200 && res.data.data && res.data.data.url) {
                this.$refs.mdEditor.$img2Url(pos, res.data.data.url);
                this.$message.success('图片上传成功');
            } else {
                this.$message.error(res.data.msg || '图片上传失败');
                this.$refs.mdEditor.$refs.toolbar_left.$imgDel(pos);
            }
        }).catch(err => {
            console.error("图片上传错误:", err.response || err);
            this.$message.error('图片上传请求失败，请检查网络或联系管理员');
            this.$refs.mdEditor.$refs.toolbar_left.$imgDel(pos);
        });
    },
    handleCategoryChange(selectedCategoryIds) {
      // console.log('选择的分类 IDs:', selectedCategoryIds);
    },
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.submitLoading = true;
          this.$axios.post("/blog/edit", this.ruleForm) 
            .then((res) => {
              if (res.data.code === 200) {
                this.$message.success(this.ruleForm.id ? "博客更新成功！" : "博客发布成功！");
                if (this.isAdmin) {
                  this.$router.push({ name: 'AdminBlogManagement' }); 
                } else {
                  this.$router.push({ name: 'UserPage' }); 
                }
              } else {
                this.$message.error(res.data.msg || "操作失败");
              }
            }).catch(error => {
              console.error("提交博客失败:", error.response || error);
              this.$message.error(error.response?.data?.msg || "提交失败，请稍后再试");
            }).finally(() => {
              this.submitLoading = false;
            });
        } else {
          console.log('表单校验失败!!');
          this.$message.error('请检查表单填写是否完整且正确');
          return false;
        }
      });
    },
    async handleTagChange(selectedTagValues) {
      const newTagNames = selectedTagValues.filter(t => typeof t === 'string');
      const existingTagIds = selectedTagValues.filter(t => typeof t !== 'string');

      if (newTagNames.length > 0) {
        const createdTagsPromises = newTagNames.map(async tagName => {
          try {
            const res = await this.$axios.post('/tags/create', { name: tagName }); 
            if (res.data.code === 200 && res.data.data) {
              const newTag = res.data.data;
              if (!this.tags.find(t => t.id === newTag.id)) {
                this.tags.push(newTag); 
              }
              return newTag.id;
            } else {
              this.$message.error(`创建标签 "${tagName}" 失败: ${res.data.msg || '未知错误'}`);
              return null;
            }
          } catch (error) {
            console.error(`创建标签 "${tagName}" 失败:`, error.response || error);
            this.$message.error(`创建标签 "${tagName}" 时发生网络错误`);
            return null;
          }
        });
        const createdTagIds = (await Promise.all(createdTagsPromises)).filter(id => id !== null);
        this.ruleForm.tagIds = [...existingTagIds, ...createdTagIds]; 
      } else {
        this.ruleForm.tagIds = existingTagIds;
      }
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
      this.ruleForm.id = null;
      this.ruleForm.categoryIds = [];
      this.ruleForm.tagIds = [];
      this.ruleForm.content = ""; 
    },
    async getGemmaSuggestions() {
      if (!this.ruleForm.content || !this.ruleForm.content.trim()) { 
        this.$message.warning("请输入一些内容以获取AI建议");
        return;
      }
      this.aiLoading = true;
      this.dialogVisible = true;
      this.suggestions = ""; 

      try {
        const response = await this.$axios.post('/api/ai/ask', {
          prompt: `请针对以下博客内容提供写作建议、改进点或相关补充信息，使其更完善和吸引人（请直接返回建议内容，不要包含其他额外说明）：\n\n${this.ruleForm.content}`
        });
        if (response.data.code === 200 && response.data.data) {
          this.suggestions = response.data.data;
        } else {
          this.suggestions = `获取建议失败: ${response.data.msg || 'AI服务未返回有效建议'}`;
        }
      } catch (error) {
        console.error("获取AI建议失败:", error.response || error);
        let errorMsg = error.response?.data?.msg || error.message || "未知错误";
        if (error.message && error.message.toLowerCase().includes("timeout")) {
          errorMsg = "请求AI服务超时，请稍后再试";
        }
        this.suggestions = `获取建议失败: ${errorMsg}`;
      } finally {
        this.aiLoading = false;
      }
    },
    applySuggestion() {
      if (this.suggestions && !this.suggestions.startsWith("获取建议失败")) {
        this.ruleForm.content += `\n\n---\n**AI 建议参考:**\n${this.suggestions}`;
        this.$message.success("建议已添加到编辑区末尾");
      }
      this.dialogVisible = false;
    },
    async fetchInitialData() {
      this.pageLoading = true; // 页面开始加载
      try {
        // 并行获取分类和标签
        const [categoriesRes, tagsRes] = await Promise.all([
          this.$axios.get('/categories'),
          this.$axios.get('/tags')
        ]);

        if (categoriesRes.data.code === 200) {
          this.categories = categoriesRes.data.data || [];
        } else {
          this.$message.error(categoriesRes.data.msg || '加载分类列表失败');
        }
        if (tagsRes.data.code === 200) {
          this.tags = tagsRes.data.data || [];
        } else {
          this.$message.error(tagsRes.data.msg || '加载标签列表失败');
        }

        // 如果是编辑模式，再获取博客数据
        const blogId = this.$route.params.blogId;
        if (blogId) {
          const blogRes = await this.$axios.get("/blog/" + blogId);
          if (blogRes.data.code === 200 && blogRes.data.data) {
            const blog = blogRes.data.data;
            this.ruleForm = {
              id: blog.id,
              title: blog.title || "",
              description: blog.description || "",
              content: blog.content || "",
              categoryIds: blog.categoryIds || (blog.categories ? blog.categories.map(c => c.id) : []),
              tagIds: blog.tagIds || (blog.tags ? blog.tags.map(t => t.id) : [])
            };
          } else {
            this.$message.error(blogRes.data.msg || "加载博客内容失败");
            this.$router.push({ name: this.isAdmin ? 'AdminBlogManagement' : 'UserPage' });
          }
        } else {
          this.ruleForm = { id: null, title: "", description: "", content: "", categoryIds: [], tagIds: [] };
        }
      } catch (error) {
        console.error("Error during initial data fetch for BlogEdit:", error.response || error);
        this.$message.error('加载编辑页面初始数据时发生网络错误');
      } finally {
        this.pageLoading = false; // 页面加载完成
      }
    },
  },
  created() {
    this.fetchInitialData(); 
  }
};
</script>

<style scoped>
.m-content {
  text-align: center;
  max-width: 960px; 
  margin: 20px auto; 
  padding: 25px;
  background-color: #fff; 
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15); 
}
.form-loading-container {
  min-height: 400px; 
  position: relative;
}

.loading-spinner {
  text-align: center;
  padding: 50px;
  font-size: 16px;
  color: #666;
}
.ai-suggestions-content .v-note-panel {
  box-shadow: none !important;
}

.el-dialog__body {
  padding: 10px 20px 20px 20px; 
}

.el-form-item {
  text-align: left; 
  margin-bottom: 22px; 
}

.mavon-editor {
  width: 100%;
  min-height: 450px; 
  border: 1px solid #dcdfe6; 
  border-radius: 4px;
}
.el-select, .el-input, .el-textarea { 
    width: 100%;
}
.el-form-item .el-button + .el-button {
    margin-left: 10px;
}
</style>
