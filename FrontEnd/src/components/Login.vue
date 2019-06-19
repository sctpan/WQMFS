<template>
  <div class="content">
    <div class="title">
      水质管理与预测系统
    </div>
    <div id="login">
      <div class="login_title">用户登录</div>
      <Form ref="user" :model="user" :rules="ruleInline">
        <FormItem prop="username">
          <Input type="text" v-model="user.username" placeholder="用户名">
            <Icon type="ios-person-outline" slot="prepend"></Icon>
          </Input>
        </FormItem>
        <FormItem prop="password" id="password">
          <Input type="password" v-model="user.password" placeholder="密码">
            <Icon type="ios-lock-outline" slot="prepend"></Icon>
          </Input>
        </FormItem>
        <Row class="register">
            <a @click="modal_flag = true">没有账号？点击注册</a>
        </Row>
        <FormItem id="submitButton">
          <Button type="primary" size="large" @click="handleSubmit('user')">登录</Button>
        </FormItem>
      </Form>
    </div>
    <div class="footer">
      <span> &copy;南京邮电大学计算机学院、软件学院、网络空间安全学院</span>
    </div>
    <Modal v-model="modal_flag" title="用户注册" cancel-text="取消" ok-text="注册" @on-ok="register" >
      <Row class="editLine">
        <Col span="4" offset="3">
          <span>用户名</span>
        </Col>
        <Col span="8" offset="1">
          <Input v-model="newUser.username"></Input>
        </Col>
      </Row>
      <Row class="editLine">
        <Col span="4" offset="3">
          <span>设置密码</span>
        </Col>
        <Col span="8" offset="1">
          <Input type="password" v-model="newUser.password"></Input>
        </Col>
      </Row>
      <Row class="editLine">
        <Col span="4" offset="3">
          <span>确认密码</span>
        </Col>
        <Col span="8" offset="1">
          <Input type="password" v-model="newUser.confirmPassword"></Input>
        </Col>
      </Row>
    </Modal>
  </div>
</template>

<script>
    export default {
        name: "Login",
        data() {
          return {
            user: {
              username: '',
              password: ''
            },
            ruleInline: {
              username: [
                { required: true, message: '请输入用户名', trigger: 'blur' }
              ],
              password: [
                { required: true, message: '请输入密码', trigger: 'blur' },
              ]
            },
            modal_flag: false,
            newUser: {
              username: "",
              password: "",
              confirmPassword: ""
            }
          }
        },
        methods: {
          handleSubmit(user) {
            let _this = this;
            this.$refs[user].validate((valid) => {
              if (valid) {
                _this.axios({
                  url: "/login",
                  method: 'POST',
                  data: {
                    username: _this.user.username,
                    password: _this.user.password
                  },
                  transformRequest: [function (data) {
                    // Do whatever you want to transform the data
                    let ret = ''
                    for (let it in data) {
                      ret += encodeURIComponent(it) + '=' + encodeURIComponent(data[it]) + '&'
                    }
                    return ret
                  }],
                  headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                  }
                })
                  .then(function(response) {
                    console.log(response.data)
                    if(response.data.status === "success") {
                      _this.$router.replace({path: '/manage'});
                    } else {
                      _this.$Message.error('账号或密码错误!');
                    }
                  })
              } else {
                this.$Message.error('账号或密码错误!');
              }
            })
          },

          check() {
            if(this.newUser.username === '' || this.newUser.password === '' || this.newUser.confirmPassword === ''){
              this.$Message.error("输入不能为空");
              return false;
            }
            if(this.newUser.password !== this.newUser.confirmPassword) {
              this.$Message.error("密码不一致");
              return false;
            }
            return true;
          },

          register() {
            if(this.check()) {
              let _this = this;
              this.axios.post("/user/register", this.qs.stringify({
                username: _this.newUser.username,
                password: _this.newUser.password
              }))
                .then(function(response) {
                  if(response.data.status === "success") {
                    _this.$Message.success("注册成功");
                  } else if(response.data.status === "duplicate") {
                    _this.$Message.error("用户名已存在");
                  } else {
                    _this.$Message.error("注册失败");
                  }
                })
            }
            this.newUser.username = "";
            this.newUser.password = "";
            this.newUser.confirmPassword = "";
          }
        }
    }
</script>

<style scoped>
  #login{
    font-size: 14px;
    margin: 0 auto;
    margin-top: 80px;
    padding: 50px;
    width: 360px;
    height: 280px;
    box-shadow: darkgrey 2px 2px 10px 2px;
    background: #ffffff;
  }

  .title {
    margin-top: 80px;
    font-size: 46px;
    color: white;
  }

  .login_title {
    font-size: 20px;
    font-weight: bold;
    color: #3399FF;
    margin-bottom: 15px;
  }

  #login img{
    margin-bottom: 20px;
  }

  #submitButton{
    margin-top: 20px;
  }

  .footer {
    font-size: 16px;
    margin-top: 100px;
    text-align: center;
  }

  #password {
    margin-bottom: 10px;
  }

  #submitButton {
    margin-top: 10px;
  }

  .editLine {
    font-size: 16px;
    line-height: 32px;
    margin-top: 10px;
    margin-bottom: 10px;
  }

</style>
