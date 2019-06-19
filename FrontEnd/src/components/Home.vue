<template>
  <div class="layout">
    <Layout :style="{minHeight: '100vh'}">
        <Header class="title" style="z-index: 999">
          <Row>
            <Col span="8" style="text-align: left; line-height: 54px">水质管理与预测系统</Col>
            <Col span="4" offset="12" style="line-height: 54px">
              <Dropdown @on-click="dropDownClicked">
                <span href="javascript:void(0)">
                  {{currentUser.username}}
                  <Icon type="ios-arrow-down"></Icon>
                </span>
                <DropdownMenu slot="list">
                  <DropdownItem name="info">用户信息</DropdownItem>
                  <DropdownItem name="editPassword">修改密码</DropdownItem>
                  <DropdownItem name="logout">注销</DropdownItem>
                </DropdownMenu>
              </Dropdown>
            </Col>
          </Row>
        </Header>
      <Layout>
        <Sider class="sider">
          <Menu class="menu" :open-names="['1','2','3']" theme="light" width="auto" :active-name="activeName">
            <Submenu name="1">
              <template slot="title">
                <Icon type="ios-archive" />
                水质数据管理
              </template>
              <MenuItem name="/recent" to="/recent">近日监测</MenuItem>
              <MenuItem name="/manage" to="/manage">历史数据</MenuItem>
              <MenuItem name="/trend" to="trend">数据趋势</MenuItem>
            </Submenu>

            <Submenu name="2">
              <template slot="title">
                <Icon type="md-trending-up" />
                水质数据预测
              </template>
              <MenuItem name="/predict" to="predict">下月预测</MenuItem>
              <MenuItem name="/train" to="train">模型训练</MenuItem>
            </Submenu>

            <Submenu name="3" style="display: none" id="user_menu">
              <template slot="title">
                <Icon type="ios-contact" />
                系统用户管理
              </template>
              <MenuItem name="/user" to="user">用户管理</MenuItem>
            </Submenu>
          </Menu>
        </Sider>
        <Layout>
          <Content>
            <router-view></router-view>
          </Content>
          <Footer class="footer">
            &copy; 南京邮电大学计算机学院、软件学院、网络空间安全学院
          </Footer>
        </Layout>
      </Layout>
    </Layout>
    <Modal v-model="info_modal_flag" title="用户信息" cancel-text="">
      <Row class="editLine">
        <Col span="4" offset="3" style="text-align: left">
          <span>用户名</span>
        </Col>
        <Col span="6" offset="1">
          <span>{{currentUser.username}}</span>
        </Col>
      </Row>
      <Row class="editLine">
        <Col span="4" offset="3" style="text-align: left">
          <span>用户角色</span>
        </Col>
        <Col span="6" offset="1">
          <span>{{currentUser.role}}</span>
        </Col>
      </Row>
      <Row class="editLine">
        <Col span="4" offset="3" style="text-align: left">
          <span>用户权限</span>
        </Col>
        <Col span="12" offset="1">
          <span>{{currentUser.authority}}</span>
        </Col>
      </Row>
    </Modal>
    <Modal v-model="pwd_modal_flag" title="修改密码" cancel-text="取消" ok-text="提交" @on-ok="editPassword" >
      <Row class="editLine">
        <Col span="4" offset="3">
          <span>原密码</span>
        </Col>
        <Col span="8" offset="1">
          <Input size="small" type="password" v-model="password.originPassword"></Input>
        </Col>
      </Row>
      <Row class="editLine">
        <Col span="4" offset="3">
          <span>新密码</span>
        </Col>
        <Col span="8" offset="1">
          <Input size="small" type="password" v-model="password.newPassword"></Input>
        </Col>
      </Row>
      <Row class="editLine">
        <Col span="4" offset="3">
          <span>确认密码</span>
        </Col>
        <Col span="8" offset="1">
          <Input size="small" type="password" v-model="password.confirmPassword"></Input>
        </Col>
      </Row>
    </Modal>
  </div>
</template>

<script>
    export default {
        name: "Home",
        data(){
          return {
            activeName: this.$route.path,
            currentUser: {},
            password: {
              originPassword: "",
              newPassword: "",
              confirmPassword: ""
            },
            info_modal_flag: false,
            pwd_modal_flag: false,
            admin_flag: false
          }
        },
        watch:{
          '$route'(){
            this.activeName = this.$route.path
          }
        },
        created() {
          this.getCurrentUser();
        },
        methods: {
          getCurrentUser() {
            let _this = this;
            this.axios.get("/user/current")
              .then(function(response) {
                if(response.data.status === "noLogin") {
                  _this.$router.replace({path: '/'});
                } else {
                  _this.currentUser = response.data;
                  if(_this.currentUser.role.id === 1) {
                    _this.currentUser.role = "超级管理员";
                    _this.currentUser.authority = "全部权限"
                    let user_menu = document.getElementById("user_menu");
                    user_menu.style.display = "block";
                  } else if(_this.currentUser.role.id === 2) {
                    _this.currentUser.role = "管理员";
                    _this.currentUser.authority = "水质数据查询、增加、修改、删除、预测; 模型训练"
                  } else {
                    _this.currentUser.role = "普通用户";
                    _this.currentUser.authority = "水质数据查询、预测"
                  }
                }
            })
          },

          dropDownClicked(name) {
            switch(name) {
              case "logout": this.logout(); break;
              case "info": this.info_modal_flag = true; break;
              case "editPassword": this.pwd_modal_flag = true; break;
            }
          },

          logout() {
            let _this = this;
            this.axios.get("/logout")
              .then(function (response) {
                if(response.data.status === "success") {
                  _this.$router.replace({path: '/'});
                } else {
                  _this.$Message.error("注销失败!")
                }
              })
          },

          editPassword() {
            let _this = this;
            if(this.checkPassword()) {
              this.axios.post("/user/editPassword/" + this.currentUser.id.toString(), this.qs.stringify({
                originPassword: _this.password.originPassword,
                newPassword: _this.password.newPassword
              }))
                .then(function(response) {
                  if(response.data.status === "success") {
                    _this.$Message.success("密码修改成功");
                  } else if(response.data.status === "error") {
                    _this.$Message.error("原密码错误");
                  } else {
                    _this.$Message.error("密码修改失败");
                  }
                })
            }
            this.password.originPassword = '';
            this.password.newPassword = '';
            this.password.confirmPassword = '';
          },

          checkPassword() {
            if(this.password.originPassword === '' || this.password.newPassword === ''
              || this.password.confirmPassword === '') {
              this.$Message.error("输入不得为空");
              return false;
            } else if(this.password.newPassword !== this.password.confirmPassword) {
              this.$Message.error("新密码不一致");
              return false;
            }
            return true;
          }
        }
    }
</script>

<style scoped>
  .title {
    font-size: 20px;
    color: #ffffff;
    background-color: #336699;
    opacity: 0.9;
    height: 54px;
  }

  .sider {
    background-color: #E8E8E8;
  }

  .menu {
    background-color: #E8E8E8;
  }

  .editLine {
    font-size: 16px;
    line-height: 24px;
    margin-top: 10px;
    margin-bottom: 10px;
  }


</style>
