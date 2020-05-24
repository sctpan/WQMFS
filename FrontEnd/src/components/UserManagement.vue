<template>
  <div class="content">
    <Row class="search">
      <Col span="6" offset="9">
        <Input search enter-button placeholder="Search User" v-model="searchUsername" @on-search="getQueriedUsers"/>
      </Col>
    </Row>
    <div class="data_table">
      <Table class="table" border  :loading="loading_flag" :columns="columns" :data="allUsers">
        <template slot-scope="{ row, index }" slot="action">
          <Button type="info" size="default" style="margin-right: 10px" @click="show(index)">Authorize</Button>
          <Button type="error" size="default" @click="remove(index)">Delete</Button>
        </template>
      </Table>
    </div>
    <Modal v-model="modal_flag" title="Authorization" cancel-text="Cancel" ok-text="submit" @on-ok="grantAuthority" >
      <Row class="editLine">
        <Col span="4" offset="6" style="text-align: center">
          <span>Username</span>
        </Col>
        <Col span="6" offset="1">
          <span>{{chosenUser.username}}</span>
        </Col>
      </Row>
      <Row class="editLine">
        <Col span="4" offset="6" style="text-align: center">
          <span>Role</span>
        </Col>
        <Col span="6" offset="1">
          <span>{{chosenUser.role}}</span>
        </Col>
      </Row>
      <Row class="editLine">
        <Col span="4" offset="6" style="text-align: center">
          <span>Authorization</span>
        </Col>
        <Col span="6" offset="1">
          <Select size="small" v-model="roleName">
            <Option value="vip" >Administrator</Option>
            <Option value="user">User</Option>
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
                title: 'ID',
                key: 'id',
                align: 'center'
              },
              {
                title: 'Username',
                key: 'username',
                align: 'center',
              },
              {
                title: 'Role',
                key: 'role',
                align: 'center',
                filters: [
                  {
                    label: 'Admin',
                    value: 1
                  },
                  {
                    label: 'User',
                    value: 2
                  }
                ],
                filterMultiple: false,
                filterMethod (value, row) {
                  if (value === 1) {
                    return row.role === "Admin";
                  } else if (value === 2) {
                    return row.role === "User";
                  }
                }
              },
              {
                title: 'Operation',
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
                    _this.allUsers[i].role = "Super Admin";
                  } else if(_this.allUsers[i].role.id === 2) {
                    _this.allUsers[i].role = "Admin";
                  } else {
                    _this.allUsers[i].role = "User";
                  }
                }
                _this.loading_flag = false;
              })
          },

          show(index) {
            this.chosenUser = this.allUsers[index];
            if(this.chosenUser.role === "Super Admin") {
                this.$Message.error("Unable to authorize the super administrator")
            } else {
                this.modal_flag = true;
            }
          },

          remove(index) {
            let _this = this;
            this.chosenUser = this.allUsers[index];
            if(this.chosenUser.role === "Super Admin") {
              this.$Message.error("Unable to delete the super administrator")
            } else {
              _this.axios.post("/user/delete/" + _this.chosenUser.id)
                .then(function(response) {
                  if(response.data.status === "success") {
                    _this.$Message.success("Delete succeed!");
                    _this.getQueriedUsers();
                  } else if(response.data.status === "deny") {
                    _this.$Message.error("Permission denied, please contact the administrator");
                  } else {
                    _this.$Message.error("Delete failed!");
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
                  _this.$Message.success("Authorize succeed!");
                  _this.getQueriedUsers();
                } else if(response.data.status === "deny") {
                  _this.$Message.error("Permission denied, please contact the administrator!");
                } else {
                  _this.$Message.error("Delete failed!");
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
                      _this.allUsers[i].role = "Super Admin";
                    } else if(_this.allUsers[i].role.id === 2) {
                      _this.allUsers[i].role = "Admin";
                    } else {
                      _this.allUsers[i].role = "User";
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
