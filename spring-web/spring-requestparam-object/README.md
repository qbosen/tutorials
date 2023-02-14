## Spring MVC  如何复用 requestParam 到 Object spring

- [How to bind @RequestParam to object in Spring | Dev in Web](http://dolszewski.com/spring/how-to-bind-requestparam-to-object/)
- 背景
    - 过多过长的参数列表
    - 无法复用；eg 和列表查询相同条件的导出操作
    - JAX-RS 中有 BeanParam 这样的支持
- 需要确认的问题
    - Spring MVC 正常工作
    - OpenAPI 相关工具 正常工作
    - name strategy 问题
- 测试工程
    - https://github.com/qbosen/tutorials/spring-web/spring-requestparam-object

### 总结

- 直接当个普通model 使用即可，但是需要保证 有无参构造和setter方法，也可以强行指定通过 field进行绑定

  ``` java
  @ControllerAdvice
  class BindingControllerAdvice {
   
     @InitBinder
     public void initBinder(WebDataBinder binder) {
         binder.initDirectFieldAccess();
     }
   
  }
  ```
  
- **命名策略问题** 通过 filter 将 snake_case 的参数转换一下，再转交 spring 进行bean映射

- JSONPath 语法
    - [JSONPath Syntax | AlertSite Documentation](https://support.smartbear.com/alertsite/docs/monitors/api/endpoint/jsonpath.html)
    - 遇到非法的property 通过 `['property']` 进行转义  
      eg: openApi 中的 `jsonPath("$.paths.['/request-object']").exists()`