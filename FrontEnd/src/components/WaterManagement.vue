<template>
  <div class="content">
    <div class="time_picker">
      <Row class="queryline">
        <Col span="2" offset="1" style="text-align: center">监测站</Col>
        <Col span="2" style="text-align: left">
          <Select v-model="station" >
            <Option value="-1">全部</Option>
            <Option v-for="item in stationList" :value="item" :key="item">{{ item }}</Option>
          </Select>
        </Col>
        <Col span="2" style="text-align: center;margin-left: 15px"><span>起始日期</span></Col>
        <Col span="4" style="text-align: left">
          <DatePicker v-if="date_loaded_flag" type="date"  @on-change="changeStartDate"
                      :start-date="startDate" :placeholder="startDateStr" ></DatePicker>
        </Col>
        <Col span="2" style="text-align: center;margin-left: 15px"><span>结束日期</span></Col>
        <Col span="4" style="text-align: left">
          <DatePicker v-if="date_loaded_flag" type="date"  @on-change="changeEndDate"
                      :start-date="endDate" :placeholder="endDateStr" ></DatePicker>
        </Col>
        <Col span="3" offset="1">
          <Button type="success" shape="circle" icon="ios-search" long @click="getQueriedWaterQualities(1)">查询</Button>
        </Col>
      </Row>
    </div>
    <div class="data_table">
      <Table class="table" border  :loading="loading_flag" :columns="columns" :data="showWaterQualities" @on-sort-change="sortBydate">
        <template slot-scope="{ row, index }" slot="action">
              <Button type="info" size="default" style="margin-right: 10px" @click="show(index)">编辑</Button>
              <Button type="error" size="default" @click="remove(index)">删除</Button>
        </template>
      </Table>
    </div>
    <div class="page">
      <template>
        <Page :current="currentPage" :total="waterQualities.length" show-elevator @on-change="changePage"/>
      </template>
    </div>
    <Modal v-model="modal_flag" title="水质数据编辑" cancel-text="取消" ok-text="提交" @on-ok="updateWaterQuality" >
          <Row class="editLine_time">
            <Col span="4" offset="7" style="text-align: center">
              <span>监测日期</span>
            </Col>
            <Col span="6">
              <span>{{chosenWaterQuality.date}}</span>
            </Col>
          </Row>
          <Row class="editLine">
            <Col span="4" offset="1" style="text-align: center">
              <span>酸碱度</span>
            </Col>
            <Col span="6">
                <Input v-model="chosenWaterQuality.ph" :placeholder="chosenWaterQuality.ph.toString()"></Input>
            </Col>
            <Col span="4" offset="1" style="text-align: center">
              <span>溶解氧</span>
            </Col>
            <Col span="6" >
              <Input v-model="chosenWaterQuality.do" :placeholder="chosenWaterQuality.do.toString()"></Input>
            </Col>
          </Row>
          <Row class="editLine">
            <Col span="4" offset="1" style="text-align: center">
              <span>氨氮</span>
            </Col>
            <Col span="6">
              <Input v-model="chosenWaterQuality.nh3N" :placeholder="chosenWaterQuality.nh3N.toString()"></Input>
            </Col>
            <Col span="4" offset="1" style="text-align: center">
              <span>监测站</span>
            </Col>
            <Col span="6">
                <Select v-model="chosenWaterQuality.station">
                  <Option v-for="item in stationList" :value="item" :key="item">{{ item }}</Option>
                </Select>
            </Col>
          </Row>
    </Modal>
  </div>
</template>

<script>
    Date.prototype.Format = function(fmt) {
      var o = {
        "M+" : this.getMonth()+1,                 //月份
        "d+" : this.getDate(),                    //日
        "h+" : this.getHours(),                   //小时
        "m+" : this.getMinutes(),                 //分
        "s+" : this.getSeconds(),                 //秒
        "q+" : Math.floor((this.getMonth()+3)/3), //季度
        "S"  : this.getMilliseconds()             //毫秒
      };
      if(/(y+)/.test(fmt))
        fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
      for(var k in o)
        if(new RegExp("("+ k +")").test(fmt))
          fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
      return fmt;
    }

    function cloneObj(obj) {
      var newObj = {};
      if (obj instanceof Array) {
        newObj = [];
      }
      for (var key in obj) {
        var val = obj[key];
        newObj[key] = typeof val === 'object' ? cloneObj(val): val;
      }
      return newObj;
    }

    export default {
        name: "WaterManagement",
        data() {
          return {
            waterQualities: [],
            showWaterQualities: [],
            stationList: [],
            currentPage: 1,
            startDate: new Date(),
            endDate: new Date(),
            startDateStr: "",
            endDateStr: "",
            station: "-1",
            chosenWaterQuality: {
              ph: "",
              do: "",
              nh3N: "",
              date: "",
              station: ""
            },
            date_loaded_flag: false,
            loading_flag: true,
            modal_flag: false,
            columns: [
              {
                title: '酸碱度（PH）',
                key: 'ph',
                align: 'center',
                // width: 160
              },
              {
                title: '溶解氧（DO mg/L）',
                key: 'do',
                align: 'center',
                // width: 160
              },
              {
                key: 'nh3N',
                renderHeader:(h,params)=>{
                  return h('div',[
                    h('span', '氨氮（'),
                    h('span', 'NH'),
                    h('sub','3'),
                    h('span','N mg/L）')
                  ]);
                },
                align: 'center',
                // width: 160
              },

              {
                title: '监测日期',
                key: 'date',
                // width: 180,
                align: 'center',
                sortable: 'custom'
              },
              {
                title: '监测站编号',
                key: 'station',
                width: 100,
                align: 'center',
              },
              {
                title: '操作',
                slot: 'action',
                align: 'center',
                 width: 200
              }
            ]

          }
        },

        created() {
          this.getAllWaterQualities();
        },

        methods: {
          getAllStations() {
            for(var i=0; i<this.waterQualities.length; i++) {
              if(this.stationList.indexOf(this.waterQualities[i].station) === -1) {
                this.stationList.push(this.waterQualities[i].station)
              }
            }
          },
          getStartAndEndTime() {
            this.startDate = new Date(this.waterQualities[0].date)
            this.endDate = new Date(this.waterQualities[this.waterQualities.length-1].date)
            this.startDateStr = this.startDate.Format("yyyy-MM-dd");
            this.endDateStr = this.endDate.Format("yyyy-MM-dd");
            this.date_loaded_flag = true
          },
          getAllWaterQualities() {
            let _this = this
            this.loading_flag = true;
            this.axios.get('/waterquality/all')
              .then(function (response) {
                _this.waterQualities = response.data
                for(var i=0; i<_this.waterQualities.length; i++) {
                  _this.waterQualities[i].date = new Date(_this.waterQualities[i].date)
                    .Format("yyyy-MM-dd hh:mm:ss")
                }
                _this.getAllStations();
                _this.getStartAndEndTime();
                _this.showWaterQualities = _this.waterQualities.slice(0, 10);
                _this.loading_flag = false;

              });
          },
          getQueriedWaterQualities(mode=2) {
            let _this = this
            this.loading_flag = true;
            let temp = new Date(this.endDateStr);
            temp.setDate(temp.getDate() + 1);
            console.log(_this.startDate)
            this.axios.get('/waterquality/query', {
              params: {
                station: _this.station,
                startDate: _this.startDateStr,
                endDate: temp.Format("yyyy-MM-dd")
              }
            })
              .then(function (response) {
                _this.waterQualities = response.data
                for(var i=0; i<_this.waterQualities.length; i++) {
                  _this.waterQualities[i].date = new Date(_this.waterQualities[i].date)
                    .Format("yyyy-MM-dd hh:mm:ss")
                }
                if(mode === 1) {
                  _this.currentPage = 1;
                }
                _this.showWaterQualities = _this.waterQualities.slice((_this.currentPage-1) * 10, _this.currentPage * 10);
                _this.loading_flag = false;
              });
          },
          updateWaterQuality() {
            let _this = this
            this.axios({
                url: '/waterquality/update/' + _this.chosenWaterQuality.id.toString(),
                method: 'POST',
                data: _this.qs.stringify({
                  PH: this.chosenWaterQuality.ph,
                  DO: this.chosenWaterQuality.do,
                  NH3N: this.chosenWaterQuality.nh3N,
                  date: new Date(this.chosenWaterQuality.date).Format("yyyy-MM-dd hh:mm:ss"),
                  station: this.chosenWaterQuality.station,
                })
            })
              .then(function (response) {
                if(response.data.status === "success") {
                  _this.$Message.success('修改成功');
                  _this.getQueriedWaterQualities();
                } else if(response.data.status === "deny"){
                  _this.$Message.error('权限不足，请联系管理员');
                } else {
                  _this.$Message.error('修改失败');
                }
              });
          },
          changePage(index) {
            this.currentPage = index;
            this.showWaterQualities = this.waterQualities.slice((index-1) * 10, index * 10);
          },
          asc_sorter(a, b) {
            if(a.date < b.date) {
              return -1;
            } else if (a.date > b.date) {
              return 1;
            } else {
              return 0;
            }
          },
          desc_sorter(a, b) {
            if(a.date < b.date) {
              return 1;
            } else if (a.date > b.date) {
              return -1;
            } else {
              return 0;
            }
          },
          sortBydate(value) {
            if(value.order === "asc") {
              this.waterQualities.sort(this.asc_sorter);
            } else {
              this.waterQualities.sort(this.desc_sorter)
            }
            this.changePage(this.currentPage);
          },
          changeStartDate(date) {
            this.startDateStr = date;
          },
          changeEndDate(date) {
            this.endDateStr = date;
          },
          show(index) {
            this.chosenWaterQuality = cloneObj(this.showWaterQualities[index]);
            this.modal_flag = true;
          },
          remove(index) {
            let _this = this
            this.chosenWaterQuality = this.showWaterQualities[index];
            this.axios.post('/waterquality/delete/' + this.chosenWaterQuality.id.toString())
              .then(function (response) {
                if(response.data.status === "success") {
                  _this.$Message.success('删除成功');
                  _this.getQueriedWaterQualities();
                } else if(response.data.status === "deny"){
                  _this.$Message.error('权限不足，请联系管理员');
                } else {
                  _this.$Message.error('删除失败');

                }
              });
          }
        }
    }
</script>

<style scoped>
  .data_table {
    margin-top: 20px;
    margin-bottom: 10px;
    padding-left: 30px;
    padding-right: 30px;
  }

  .time_picker {
    margin-top: 20px;
  }

  .queryline {
    line-height: 32px;
  }

  .editLine {
    line-height: 32px;
    margin-top: 10px;
    margin-bottom: 10px;
  }

  .editLine_time {
    line-height: 32px;
    margin-bottom: 10px;
  }


</style>
