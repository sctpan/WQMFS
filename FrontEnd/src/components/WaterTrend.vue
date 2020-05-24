<template>
  <div class="content">
    <div class="query">
      <Row class="queryline">
        <Col span="2" offset="2" style="text-align: right;margin-right: 15px">Station</Col>
        <Col span="2" style="text-align: left">
          <Select v-model="station" style="width:100px" placeholder="0">
            <Option v-for="item in stationList" :value="item" :key="item">{{ item }}</Option>
          </Select>
        </Col>
        <Col span="2" style="text-align: right;margin-right: 15px; margin-left: 15px"><span>Water Quality Index</span></Col>
        <Col span="2" style="text-align: left">
          <Select v-model="indicator" style="width: 100px" placeholder="PH">
            <Option value="ph">PH</Option>
            <Option value="do">Dissolved Oxygen</Option>
            <Option value="nh3N">Ammonia Nitrogen</Option>
          </Select>
        </Col>
        <Col span="2" style="text-align: right;margin-right: 15px;margin-left: 15px"><span>Time</span></Col>
        <Col span="2" style="text-align: left">
          <Select v-model="period" style="width: 100px" placeholder="One Year">
            <Option value="1">Last Year</Option>
            <Option value="3">Last 3 Years</Option>
            <Option value="5">Last 5 Years</Option>
          </Select>
        </Col>
        <Col span="3" offset="2">
          <Button type="success" shape="circle" icon="ios-stats" long @click="getQueriedDataForPlot">Show Trend</Button>
        </Col>
      </Row>
    </div>
    <div id="plot_holder" v-if="plot_loading_flag" style="height: 600px">
      <spin size="large" fix v-if="plot_loading_flag" style="font-size: 20px">Loading...</spin>
    </div>
    <div id="plot" style="height: 600px"></div>
  </div>
</template>

<script>
    export default {
        name: "WaterTrend",
        data() {
          return {
            stationList: [],
            station: 0,
            period: "1",
            indicator: "ph",
            plot_loading_flag: true,
            waterQualities: [],
            dates: []
          }
        },
        mounted() {
          this.getAllStations();
          this.getQueriedDataForPlot();
        },
        methods: {
          getAllStations() {
            let _this = this;
            this.axios.get("waterquality/station")
              .then(function(response) {
                _this.stationList = response.data;
              });
          },
          getQueriedDataForPlot() {
            let _this = this;
            let charts = document.getElementById("plot");
            charts.style.height = "0";
            this.axios.get("waterquality/plot", {
              params: {
                station: _this.station,
                period: _this.period,
                indicator: _this.indicator
              }
            })
              .then(function (response) {
                let data = response.data;
                _this.waterQualities = data.waterquality;
                _this.dates = data.dates;
                charts.style.height = "600px";
                _this.plot();
              })
          },
          plot() {
            let _this = this;
            let chart = this.echarts.init(document.getElementById("plot"));
            let option = {
              tooltip: {
                trigger: "item"
              },
              xAxis: {
                type: 'category',
                data: _this.dates
              },
              yAxis: {
                type: 'value',
                scale: true
              },
              series: [{
                data: _this.waterQualities,
                type: 'line',
              }]
            };
            chart.setOption(option);
            _this.plot_loading_flag = false;
          }

        }
    }
</script>

<style scoped>
  .query {
    margin-top: 20px;
  }

  .queryline {
    line-height: 32px;
  }

  #plot_holder {
    position: relative;
    margin-top: 20px;
    margin-left: 30px;
    margin-right: 30px;
  }

</style>
