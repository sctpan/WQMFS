<template>
  <div class="content">
    <div class="query">
      <Row class="queryline">
        <Col span="2" offset="1" style="text-align: center">Water Quality Index</Col>
        <Col span="3" style="text-align: left">
          <Select v-model="indicator">
            <Option value="ph">PH</Option>
            <Option value="do">Dissolved Oxygen</Option>
            <Option value="nh3N">Ammonia Nitrogen</Option>
          </Select>
        </Col>
        <Col span="3" offset="1" style="text-align: center">Machine Learning Model</Col>
        <Col span="3" style="text-align: left">
          <Select v-model="method">
            <Option value="LSTM">LSTM</Option>
            <Option value="SVM">SVM</Option>
            <Option value="RVM">RVM</Option>
            <Option value="Adaboost">Adaboost</Option>
            <Option value="BP">BP</Option>
            <Option value="Optimization">Optimization</Option>
          </Select>
        </Col>
        <Col span="3" offset="2">
          <Button type="success" shape="circle" icon="ios-build" long @click="trainModel">Train Model</Button>
        </Col>
      </Row>
    </div>
    <div id="plot_holder" v-if="plot_loading_flag" style="height: 600px">
      <spin size="large" fix v-if="plot_loading_flag" style="font-size: 20px">Training {{method}} model...</spin>
    </div>
    <div id="plot" style="height: 600px"></div>
  </div>
</template>

<script>
    export default {
        name: "ModelTraining",
        data(){
          return {
            indicator: "ph",
            method: "LSTM",
            rmse: "",
            pred: [],
            real: [],
            plot_loading_flag: false,
            currentUser: {}
          }
        },
        created() {
          this.getCurrentUser();
        },
        methods: {
          trainModel() {
            let _this = this;
            let charts = document.getElementById("plot");
            charts.style.visibility = "hidden";
            charts.style.height = "0";
            _this.plot_loading_flag = true;
            this.axios.get("model/training", {
              params: {
                indicator: _this.indicator,
                method: _this.method,
                uid: _this.currentUser.id
              }
            })
              .then(function(response) {
                let body = response.data;
                if(body.status === "success") {
                  _this.rmse = body.data.rmse;
                  _this.pred = body.data.pred;
                  _this.real = body.data.real;
                  charts.style.height = "600px";
                  _this.plot();
                } else if(body.status === "deny") {
                  _this.$Message.error("\n" + "Permission denied, please contact the administrator");
                  charts.style.height = "600px";
                  charts.style.visibility = "visible"
                  _this.plot_loading_flag = false;
                } else {
                  _this.$Message.error("Training failed！");
                  charts.style.height = "600px";
                  charts.style.visibility = "visible"
                  _this.plot_loading_flag = false;
                }
              })
          },

          getCurrentUser() {
            let _this = this;
            this.axios.get("/user/current")
              .then(function(response) {
                  _this.currentUser = response.data;
              })
          },

          plot() {
            let _this = this;
            let chart = this.echarts.init(document.getElementById("plot"));
            let x = new Array(_this.pred.length);
            for(var i=0; i<_this.pred.length; i++) {
              x[i] = i + 1;
            }
            let option = {
              title: {
                left: 'center',
                text: 'Training Result of'  + ' ' + _this.method + ' Model on ' + _this.indicator.toUpperCase(),
                subtext: 'The RMSE on test set is ' + _this.rmse
              },
              legend: {
                data:['Predication','Actual value']
              },
              tooltip: {
                trigger: "item"
              },
              xAxis: {
                type: 'category',
                data: x
              },
              yAxis: {
                type: 'value',
                scale: true
              },
              series: [{
                data: _this.pred,
                itemStyle: {
                  normal: {
                    color: 'blue',
                    lineStyle: {
                      color: 'blue'
                    }
                  }
                },
                type: 'line'
              }, {
                data: _this.real,
                itemStyle: {
                  normal: {
                    color: 'red',
                    lineStyle: {
                      color: 'red'
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
    margin-bottom: 20px;
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
