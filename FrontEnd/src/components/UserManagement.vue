<template>
  <div class="content">
    <Row class="search">
      <Col span="6" offset="9">
        <Input search enter-button placeholder="搜索用户名" v-model="searchUsername" @on-search="getQueriedUsers"/>
      </Col>
    </Row>
    <div class="data_table">
      <Table class="table" border  :loading="loading_flag" :columns="columns" :data="allUsers">
        <template slot-scope="{ row, index }" slot="action">
          <Button type="info" size="default" style="margin-right: 10px" @click="show(index)">授权</Button>
          <Button type="error" size="default" @click="remove(index)">删除</Button>
        </template>
      </Table>
    </div>
    <Modal v-model="modal_flag" title="用户授权" cancel-text="取消" ok-text="提交" @on-ok="grantAuthority" >
      <Row class="editLine">
        <Col span="4" offset="6" style="text-align: center">
          <span>用户名</span>
        </Col>
        <Col span="6" offset="1">
          <span>{{chosenUser.username}}</span>
        </Col>
      </Row>
      <Row class="editLine">
        <Col span="4" offset="6" style="text-align: center">
          <span>当前权限</span>
        </Col>
        <Col span="6" offset="1">
          <span>{{chosenUser.role}}</span>
        </Col>
      </Row>
      <Row class="editLine">
        <Col span="4" offset="6" style="text-align: center">
          <span>授予权限</span>
        </Col>
        <Col span="6" offset="1">
          <Select size="small" v-model="roleName">
            <Option value="vip" >管理员</Option>
            <Option value="user">普通用户</Option>
          </Select>
        </Col>
      </Row>
    </Modal>
  </div>
</template>

<script>
    export default {
        name: "UserManagement",
        data() {
          return {
            loading_flag: true,
            modal_flag: false,
            allUsers: [],
            chosenUser: {},
            roleName: "",
            searchUsername: "",
            columns: [
              {
                title: '用户ID',
                key: 'id',
                align: 'center'
              },
              {
                title: '用户名',
                key: 'username',
                align: 'center',
              },
              {
                title: '用户角色',
                key: 'role',
                align: 'center',
                filters: [
                  {
                    label: '管理员',
                    value: 1
                  },
                  {
                    label: '普通用户',
                    value: 2
                  }
                ],
                filterMultiple: false,
                filterMethod (value, row) {
                  if (value === 1) {
                    return row.role === "管理员";
                  } else if (value === 2) {
                    return row.role === "普通用户";
                  }
                }
              },
              {
                title: '操作',
                slot: 'action',
                align: 'center',
              }
            ]
          }
        },
      created() {
          this.getAllUsers();
      },
      methods: {
          getAllUsers() {
            let _this = this;
            this.loading_flag = true;
            this.axios.get("/user/all")
              .then(function(response){
                _this.allUsers = response.data;
                for(var i=0; i<_this.allUsers.length; i++) {
                  if(_this.allUsers[i].role.id === 1) {
                    _this.allUsers[i].role = "超级管理员";
                  } else if(_this.allUsers[i].role.id === 2) {
                    _this.allUsers[i].role = "管理员";
                  } else {
                    _this.allUsers[i].role = "普通用户";
                  }
                }
                _this.loading_flag = false;
              })
          },

          show(index) {
            this.chosenUser = this.allUsers[index];
            if(this.chosenUser.role === "超级管理员") {
                this.$Message.error("无法对超级管理员进行授权")
            } else {
                this.modal_flag = true;
            }
          },

          remove(index) {
            let _this = this;
            this.chosenUser = this.allUsers[index];
            if(this.chosenUser.role === "超级管理员") {
              this.$Message.error("无法删除超级管理员")
            } else {
              _this.axios.post("/user/delete/" + _this.chosenUser.id)
                .then(function(response) {
                  if(response.data.status === "success") {
                    _this.$Message.success("删除成功");
                    _this.getQueriedUsers();
                  } else if(response.data.status === "deny") {
                    _this.$Message.error("权限不足，请联系管理员");
                  } else {
                    _this.$Message.error("删除失败");
                  }
                })
            }
          },

          grantAuthority() {
            let _this = this;
            this.axios.post("/user/grant/" + _this.chosenUser.id.toString(), _this.qs.stringify({
              roleName: _this.roleName
            }))
              .then(function (response) {
                if(response.data.status === "success") {
                  _this.$Message.success("授权成功");
                  _this.getQueriedUsers();
                } else if(response.data.status === "deny") {
                  _this.$Message.error("权限不足，请联系管理员");
                } else {
                  _this.$Message.error("授权失败");
                }
              })
          },

          getQueriedUsers() {
            let _this = this;
            this.loading_flag = true;
            if(this.searchUsername === "") {
              this.getAllUsers();
            } else {
              _this.axios.get("/user/query",{
                params:{
                  username: _this.searchUsername
                }
              })
                .then(function(response) {
                  _this.allUsers = response.data;
                  for(var i=0; i<_this.allUsers.length; i++) {
                    if(_this.allUsers[i].role.id === 1) {
                      _this.allUsers[i].role = "超级管理员";
                    } else if(_this.allUsers[i].role.id === 2) {
                      _this.allUsers[i].role = "管理员";
                    } else {
                      _this.allUsers[i].role = "普通用户";
                    }
                  }
                  _this.loading_flag = false;
                })
            }
          }
      }

    }
</script>

<style scoped>
  .search {
    font-size: 16px;
    line-height: 32px;
    margin-top: 20px;
  }

  .data_table {
    margin-top: 20px;
    margin-bottom: 10px;
    padding-left: 30px;
    padding-right: 30px;
  }

  .editLine {
    font-size: 14px;
    line-height: 24px;
    margin-top: 10px;
    margin-bottom: 10px;
  }

</style>
