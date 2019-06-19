<template>
  <div class="content">
    <div class="title">
      <span>近日监测情况</span>
    </div>
    <div class="data_table">
      <Table border  :loading="loading_flag"  :columns="columns" :data="waterQualities">
      </Table>
    </div>
    <div class="add">
      <Button type="success" shape="circle" size="large" @click="showModal">手动添加数据</Button>
    </div>
    <Modal v-model="modal_flag" title="添加水质数据" cancel-text="取消" ok-text="提交" @on-ok="addWaterQuality" >
      <Row class="editLine_time">
        <Col span="4" offset="5" style="text-align: center">
          <span>监测日期</span>
        </Col>
        <Col>
          <DatePicker type="date"  :startDate="toBeAddedWaterQuality.date" @on-change="changeDate"></DatePicker>
        </Col>
      </Row>
      <Row class="editLine">
        <Col span="4" offset="1" style="text-align: center">
          <span>酸碱度</span>
        </Col>
        <Col span="6">
          <Input v-model="toBeAddedWaterQuality.ph"></Input>
        </Col>
        <Col span="4" offset="1" style="text-align: center">
          <span>溶解氧</span>
        </Col>
        <Col span="6" >
          <Input v-model="toBeAddedWaterQuality.do"></Input>
        </Col>
      </Row>
      <Row class="editLine">
        <Col span="4" offset="1" style="text-align: center">
          <span>氨氮</span>
        </Col>
        <Col span="6">
          <Input v-model="toBeAddedWaterQuality.nh3N"></Input>
        </Col>
        <Col span="4" offset="1" style="text-align: center">
          <span>监测站</span>
        </Col>
        <Col span="6">
          <Select v-model="toBeAddedWaterQuality.station">
            <Option v-for="item in stationList" :value="item" :key="item">{{ item }}</Option>
          </Select>
        </Col>
      </Row>
    </Modal>
  </div>
</template>

<script>
    export default {
        name: "RecentCollection",
        data() {
          return {
            loading_flag: true,
            modal_flag: false,
            stationList: [],
            waterQualities: [],
            toBeAddedWaterQuality: {
              ph: "",
              do: "",
              nh3N: "",
              date: new Date(),
              station: ""
            },
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
                //width: 220,
                align: 'center'
              },
              {
                title: '监测站编号',
                key: 'station',
                width: 100,
                align: 'center',
              }
            ]
          }
        },
        created() {
          this.getAllStations();
          this.getRecentWaterQualities();
        },
        methods: {
          getAllStations() {
            let _this = this;
            this.axios.get("waterquality/station")
              .then(function(response) {
                _this.stationList = response.data;
              });
          },
          getRecentWaterQualities(num = 10) {
            let _this = this;
            this.axios.get("waterquality/recent", {
              params: {
                num: num
              }
            })
              .then(function(response) {
                _this.loading_flag = true;
                _this.waterQualities = response.data;
                for(var i=0; i<_this.waterQualities.length; i++) {
                  _this.waterQualities[i].date = new Date(_this.waterQualities[i].date)
                    .Format("yyyy-MM-dd hh:mm:ss")
                }
                _this.loading_flag = false;
              })
          },
          showModal() {
            this.modal_flag = true;
          },
          addWaterQuality() {
            let _this = this;
            this.axios.post("/waterquality/add", _this.qs.stringify({
              PH: _this.toBeAddedWaterQuality.ph,
              DO: _this.toBeAddedWaterQuality.do,
              NH3N: _this.toBeAddedWaterQuality.nh3N,
              date: _this.toBeAddedWaterQuality.date.Format("yyyy-MM-dd hh:mm:ss"),
              station: _this.toBeAddedWaterQuality.station
            }))
              .then(function(response) {
                if(response.data.status == "success") {
                  _this.$Message.success("添加成功");
                } else if(response.data.status == "deny"){
                    _this.$Message.error("权限不足，请联系管理员")
                } else {
                    _this.$Message.error("添加失败");
                }
                _this.getRecentWaterQualities();
              })
          },
          changeDate(date) {
            this.toBeAddedWaterQuality.date = new Date(date);
          }
        }
    }
</script>

<style scoped>
  .data_table {
    margin-top: 20px;
    margin-bottom: 20px;
    padding-left: 30px;
    padding-right: 30px;
  }

  .title {
    height: 32px;
    font-size: 20px;
    line-height: 32px;
    margin-top: 20px;
  }

  .editLine {
    line-height: 32px;
    margin-top: 10px;
    margin-bottom: 10px;
  }

  .editLine_time {
    line-height: 32px;
    margin-bottom: 15px;
  }

</style>
