<template>
  <div class="content">
    <div class="query">
      <Row class="queryline">
        <Col span="2" offset="1" style="text-align: center">Water Quality Index</Col>
        <Col span="3" style="text-align: left">
          <Select v-model="indicator" @on-change="getAllMethods" >
            <Option value="ph">PH</Option>
            <Option value="do">Dissolved Oxygen</Option>
            <Option value="nh3N">Ammonia Nitrogen</Option>
          </Select>
        </Col>
        <Col span="3" offset="1" style="text-align: center">Machine Learning Model</Col>
        <Col span="3" style="text-align: left">
          <Select v-model="method" @on-change="getAvailableMethods">
            <Option value="all">All</Option>
            <Option v-for="item in modelList" :value="item" :key="item">{{ item }}</Option>
          </Select>
        </Col>
        <!--<Col span="3" offset="2">-->
          <!--<Button type="success" shape="circle" icon="md-trending-up" long @click="">下月预测</Button>-->
        <!--</Col>-->
      </Row>
    </div>
    <div class="data_table">
      <div class="table_title">Available Models</div>
      <Table class="table" :loading="model_table_flag" border :columns="columns" :data="availableModels">
        <template slot-scope="{ row, index }" slot="action">
          <Button type="success" size="default" @click="getNextMonthPrediction(index)">Predict</Button>
          <Button type="error" size="default" @click="deleteModel(index)">Delete</Button>
        </template>
      </Table>
    </div>
    <div id="plot_holder" v-if="plot_loading_flag" style="height: 400px">
      <spin size="large" fix v-if="plot_loading_flag" style="font-size: 20px">Using {{chosenModel.method}} to {{indicator.toUpperCase()}} predict...</spin>
    </div>
    <div id="plot" style="height: 400px"></div>

  </div>
</template>

<script>
    export default {
        name: "WaterPrediction",
        data() {
          return {
            indicator: "ph",
            method: "all",
            modelList: [],
            availableModels: [],
            prediction: "",
            chosenModel: {},
            model_table_flag: false,
            plot_loading_flag: false,
            plotWaterQualities: [],
            plotDates: [],
            plotWaterQualitiesSecond: [],
            columns: [
              {
                title: 'Type',
                key: 'method',
                align: 'center',
              },
              {
                title: 'RMSE',
                key: 'rmse',
                align: 'center',
                sortable: 'true'
              },
              {
                title: 'Trainer',
                key: 'user',
                align: 'center',
              },
              {
                title: 'Training Date',
                key: 'date',
                align: 'center',
                sortable: 'true'
              },
              {
                title: 'Operation',
                slot: 'action',
                align: 'center',
              }
            ]
          }
        },
        mounted() {
          this.getAllMethods();
        },
        methods: {
          getAvailableMethods() {
            let _this = this;
            this.model_table_flag = true;
            var charts = document.getElementById("plot");
            charts.style.height = "0";
            charts.style.visibility = "hidden";
            this.axios.get("/model/available", {
              params: {
                indicator: _this.indicator,
                method: _this.method,
              }
            })
              .then(function(response) {
                _this.availableModels = response.data
                for(var i=0; i<_this.availableModels.length; i++) {
                  _this.availableModels[i].user = _this.availableModels[i].user.username;
                  _this.availableModels[i].date = new Date(_this.availableModels[i].date)
                    .Format("yyyy-MM-dd hh:mm:ss")
                }
                _this.model_table_flag = false;
              })
          },
          getAllMethods() {
            let _this = this;
            this.model_table_flag = true;
            var charts = document.getElementById("plot");
            charts.style.height = "0";
            charts.style.visibility = "hidden";
            this.axios.get("model/list", {
              params: {
                indicator: _this.indicator
              }
            })
              .then(function (response) {
                _this.modelList = response.data
                _this.method = "all"
                _this.getAvailableMethods()
              })
          },

          deleteModel(index) {
            let _this = this;
            this.chosenModel = this.availableModels[index];
            this.axios.post("model/delete/" + this.chosenModel.id.toString())
              .then(function (response) {
                if(response.data.status === "success") {
                  _this.$Message.success("Delete succeed!");
                  _this.getAvailableMethods();
                } else if(response.data.status === "deny"){
                  _this.$Message.error("Permission denied, please contact the administrator!");
                } else {
                  _this.$Message.error("Delete failed!");

                }
              })
          },

          getNextMonthPrediction(index) {
            let _this = this;
            this.plot_loading_flag = true;
            let charts = document.getElementById("plot");
            charts.style.height = "0";
            charts.style.visibility = "hidden"
            this.chosenModel = this.availableModels[index]
            this.axios.get("model/prediction", {
              params: {
                id: _this.chosenModel.id,
                indicator: _this.indicator
              }
            })
              .then(function(response) {
                let body = response.data
                if(body.status === "success") {
                  _this.prediction = body.pred;
                  _this.plotWaterQualities = body.forPlot;
                  _this.plotWaterQualitiesSecond = ["-","-","-","-", _this.plotWaterQualities[4], _this.prediction];
                  _this.plotWaterQualities[5] = "-";
                  _this.plotDates = body.dates;
                  charts.style.height = "400px";
                  _this.plot()
                } else {
                  charts.style.visibility = "visible"
                  charts.style.height = "400px";
                  _this.plot_loading_flag = false;
                  _this.$Message.error("Predict failed！")
                }
              })
          },
          plot() {
            let _this = this;
            let chart = this.echarts.init(document.getElementById("plot"));
            let option = {
              title: {
                left: 'center',
                text: _this.indicator.toUpperCase() + 'Predication for Next Month: ' + _this.prediction,
                subtext: 'Predicted by ' + _this.chosenModel.method + ' model ' + 'which is trained by ' + _this.chosenModel.user + " on " + _this.chosenModel.date

              },
              tooltip: {
                trigger: "item"
              },
              xAxis: {
                type: 'category',
                data: _this.plotDates
              },
              yAxis: {
                type: 'value',
                scale: true
              },
              series: [{
                data: _this.plotWaterQualities,
                itemStyle : {
                  normal : {
                    color:'blue',
                    lineStyle:{
                      color:'blue'
                    }
                  }
                },
                type: 'line'
              },{
                data: _this.plotWaterQualitiesSecond,
                itemStyle : {
                  normal : {
                    color:'green',
                    lineStyle:{
                      color:'green'
                    }
                  }
                },
                type: 'line'
              }]
            };
            chart.setOption(option);
            var charts = document.getElementById("plot");
            charts.style.visibility = "visible"
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

  .data_table {
    margin-top: 20px;
    margin-bottom: 15px;
    padding-left: 30px;
    padding-right: 30px;
  }

  .table_title {
    margin-bottom: 10px;
    font-size: 16px;
  }

  .plot_title {
    margin-bottom: 15px;
    font-size: 18px;
  }

  #plot_holder {
    position: relative;
    margin-left: 30px;
    margin-right: 30px;
  }

  /*#plot {*/
    /*position: relative;*/
  /*}*/

</style>
