<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!doctype html>
<html>
	<head>
		<title>用户列表</title>
		<base href="${webRoot}">
		<meta charset="UTF-8">
    	<meta name="theme" content="manager"/>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0,minimum-scale=1.0, user-scalable=no">
		<meta name="format-detection" content="telephone=no" />
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<link rel="stylesheet" href="${ctx}/static/css/normalize.css">
		<script type="text/javascript" src="${ctx}/static/js/jquery.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/alert.js"></script>
		<style>
		#datatable{width:100%;}
		#datatable td{height:30px;padding:0 10px;overflow:hidden;}
		</style>
	</head>
	<body>
		<select id="pages">
			<c:forEach begin="1" end="${page.totalPages}" var="p">
				<option value="${p}">第${p}页</option>
			</c:forEach>
		</select>
		<a href="${webRoot}/index">添加数据</a>
		<table border="1" id="datatable">
			<c:forEach var="user" items="${page.content}">
				<tr>
					<td>${user.id}</td>
					<td>${user.username}</td>
					<td>${user.password}</td>
					<td><a href="javascript:void(0)" class="delbtn">删除</a></td>
				</tr>
			</c:forEach>
			<c:if test="${page.size==0}"><tr><td>暂时没有任何数据</td></tr></c:if>
		</table>
	</body>
	<script type="text/javascript">
	$(function(){
		$(document).on('ajaxStart',function(){
			var ajaxoverlay=$('#jquery_global_ajax_overlay');
			if(!ajaxoverlay.length) ajaxoverlay=$('<div id="jquery_global_ajax_overlay" style="position:fixed;top:0;left:0;width:100%;height:100%;display:none;z-index:999;cursor:wait;"></div>').appendTo('body');
			ajaxoverlay.stop().fadeTo(.05);
		}).on('ajaxStop',function(){
			$('#jquery_global_ajax_overlay').stop().fadeOut('fast');
		}).on('ajaxError',function(){
			$.alert('请求失败，请稍后再试！');
		});

		$('#pages').on('change',function(){
			var page=$(this).val();
			$.post('${ctx}/ajax/list/'+page,function(data){
				var html='';
				$.each(data.list,function(i,d){
					html+='<tr><td>'+d.id+'</td><td>'+d.username+'</td><td>'+d.password+'</td><td><a href="javascript:void(0)" class="delbtn">删除</a></td></tr>'
				});
				$('#datatable').html(html);
			},'json')
		});
		$('#datatable').on('click','.delbtn',function(){
			var parent=$(this).parent().parent(),id=parent.find(':first').text();
			$.confirm('确定要删除该条数据吗？',function(r){
				if(r){
					$.post('${ctx}/ajax/delete/'+id,function(data){
						if(!data.err){
							parent.fadeOut(function(){parent.remove()});
						}else{
							$.alert(data.msg);
						}
					},'json')
				}
			})
		})
	})
	</script>
</html>
