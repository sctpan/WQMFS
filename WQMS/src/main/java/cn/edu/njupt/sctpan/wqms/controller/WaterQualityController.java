package cn.edu.njupt.sctpan.wqms.controller;

import cn.edu.njupt.sctpan.wqms.model.WaterQuality;
import cn.edu.njupt.sctpan.wqms.model.WaterQualityHelper;
import cn.edu.njupt.sctpan.wqms.service.WaterQualityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/waterquality")
public class WaterQualityController {
    @Autowired
    private WaterQualityService waterQualityService;

    @GetMapping("/query")
    public List<WaterQuality> getQueriedWaterQualities(@RequestParam(value = "station") Integer station,
       @RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate) {
       return waterQualityService.findQueriedWaterQualities(startDate, endDate, station);
    }

    @GetMapping("/all")
    public List<WaterQuality> getAllWaterQualities() {
        return waterQualityService.findAllWaterQualities();
    }

    @PostMapping("/update/{id}")
    public Map<String, String> updateWaterQuality(WaterQualityHelper waterQualityHelper, @PathVariable("id") int id) {
        WaterQuality waterQuality = waterQualityHelper.convert();
        waterQuality.setId(id);
        Map<String, String> resp = new HashMap<>();
        if(waterQualityService.updateWaterQuality(waterQuality)) {
            resp.put("status", "success");
        } else {
            resp.put("status", "fail");
        }
        return resp;
    }

    @PostMapping("/delete/{id}")
    public Map<String, String> deleteWaterQuality(@PathVariable("id") int id) {
        Map<String, String> resp = new HashMap<>();
        if(waterQualityService.deleteWaterQuality(id)) {
            resp.put("status", "success");
        } else {
            resp.put("status", "fail");
        }
        return resp;
    }

    @GetMapping("/station")
    public List<Integer> getAllStations() {
        return waterQualityService.getAllStations();
    }

    @GetMapping("/plot")
    public Map<String, Object> getDataForPlot(@RequestParam(value = "station") Integer station,
         @RequestParam(value = "period") Integer period, @RequestParam(value = "indicator") String indicator) {
        return waterQualityService.getDataForPlot(station, period, indicator);
    }

    @GetMapping("/recent")
    public List<WaterQuality> getRecentWaterQualites(@RequestParam(value = "num") Integer num) {
        return waterQualityService.getRecentWaterQualities(num);
    }

    @PostMapping("/add")
    public Map<String, String> addWaterQuality(WaterQualityHelper waterQualityHelper) {
        Map<String, String> resp = new HashMap<>();
        WaterQuality waterQuality = waterQualityHelper.convert();
        if(waterQualityService.addWaterQuality(waterQuality)) {
            resp.put("status", "success");
        } else {
            resp.put("status", "failure");
        }
        return resp;
    }


}
