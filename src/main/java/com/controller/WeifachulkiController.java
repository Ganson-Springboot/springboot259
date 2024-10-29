
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 违法处理业务
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/weifachulki")
public class WeifachulkiController {
    private static final Logger logger = LoggerFactory.getLogger(WeifachulkiController.class);

    @Autowired
    private WeifachulkiService weifachulkiService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service
    @Autowired
    private YonghuService yonghuService;



    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = weifachulkiService.queryPage(params);

        //字典表数据转换
        List<WeifachulkiView> list =(List<WeifachulkiView>)page.getList();
        for(WeifachulkiView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        WeifachulkiEntity weifachulki = weifachulkiService.selectById(id);
        if(weifachulki !=null){
            //entity转view
            WeifachulkiView view = new WeifachulkiView();
            BeanUtils.copyProperties( weifachulki , view );//把实体数据重构到view中

                //级联表
                YonghuEntity yonghu = yonghuService.selectById(weifachulki.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody WeifachulkiEntity weifachulki, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,weifachulki:{}",this.getClass().getName(),weifachulki.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("用户".equals(role))
            weifachulki.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        Wrapper<WeifachulkiEntity> queryWrapper = new EntityWrapper<WeifachulkiEntity>()
            .eq("weifachulki_name", weifachulki.getWeifachulkiName())
            .eq("weifachulki_types", weifachulki.getWeifachulkiTypes())
            .eq("weifachulki_video", weifachulki.getWeifachulkiVideo())
            .eq("yonghu_id", weifachulki.getYonghuId())
            .eq("weifachulki_status_types", weifachulki.getWeifachulkiStatusTypes())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        WeifachulkiEntity weifachulkiEntity = weifachulkiService.selectOne(queryWrapper);
        if(weifachulkiEntity==null){
            weifachulki.setCreateTime(new Date());
            weifachulkiService.insert(weifachulki);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody WeifachulkiEntity weifachulki, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,weifachulki:{}",this.getClass().getName(),weifachulki.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("用户".equals(role))
//            weifachulki.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        //根据字段查询是否有相同数据
        Wrapper<WeifachulkiEntity> queryWrapper = new EntityWrapper<WeifachulkiEntity>()
            .notIn("id",weifachulki.getId())
            .andNew()
            .eq("weifachulki_name", weifachulki.getWeifachulkiName())
            .eq("weifachulki_types", weifachulki.getWeifachulkiTypes())
            .eq("weifachulki_video", weifachulki.getWeifachulkiVideo())
            .eq("yonghu_id", weifachulki.getYonghuId())
            .eq("weifachulki_status_types", weifachulki.getWeifachulkiStatusTypes())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        WeifachulkiEntity weifachulkiEntity = weifachulkiService.selectOne(queryWrapper);
        if("".equals(weifachulki.getWeifachulkiVideo()) || "null".equals(weifachulki.getWeifachulkiVideo())){
                weifachulki.setWeifachulkiVideo(null);
        }
        if(weifachulkiEntity==null){
            weifachulkiService.updateById(weifachulki);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        weifachulkiService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<WeifachulkiEntity> weifachulkiList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            WeifachulkiEntity weifachulkiEntity = new WeifachulkiEntity();
//                            weifachulkiEntity.setWeifachulkiName(data.get(0));                    //标题 要改的
//                            weifachulkiEntity.setWeifachulkiTypes(Integer.valueOf(data.get(0)));   //违法处理业务类型 要改的
//                            weifachulkiEntity.setWeifachulkiTime(sdf.parse(data.get(0)));          //时间 要改的
//                            weifachulkiEntity.setWeifachulkiVideo(data.get(0));                    //内容 要改的
//                            weifachulkiEntity.setWeifachulkiMoney(data.get(0));                    //内容 要改的
//                            weifachulkiEntity.setYonghuId(Integer.valueOf(data.get(0)));   //用户 要改的
//                            weifachulkiEntity.setWeifachulkiContent("");//详情和图片
//                            weifachulkiEntity.setWeifachulkiStatusTypes(Integer.valueOf(data.get(0)));   //业务状态 要改的
//                            weifachulkiEntity.setCreateTime(date);//时间
                            weifachulkiList.add(weifachulkiEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        weifachulkiService.insertBatch(weifachulkiList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }






}
