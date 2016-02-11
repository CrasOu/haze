<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<html>
	<head>
		<title>添加用户</title>
		<base href="${webRoot}">
		<meta charset="UTF-8">
    	<meta name="theme" content="manager"/>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0,minimum-scale=1.0, user-scalable=no">
		<meta name="format-detection" content="telephone=no" />
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<link rel="stylesheet" href="${webRoot}/static/css/normalize.css">
		<script type="text/javascript" src="${webRoot}/static/js/jquery.js"></script>
		<script type="text/javascript" src="${webRoot}/static/js/alert.js"></script>
		<style type="text/css">
		.animate{-webkit-transition: all .3s ease-out;-ms-transition: all .3s ease-out;-moz-transition: all .3s ease-out;-o-transition: all .3s ease-out;transition: all .3s ease-out;}
		td{padding:10px;}
		input{height:30px;line-height:normal;width:240px;outline:none;border:1px solid #ccc;text-indent:5px;}
		input:focus{border:1px solid #498BF3;text-indent:2px;}
		#addbtn{color:#fff;text-decoration:none;background:#498BF3;display:inline-block;width:100px;height:40px;line-height:40px;}
		#addbtn:hover{background:#3D6BD7;border-radius:4px;}
		</style>
	</head>
	<body>
		<form id="myform" action="${webRoot}/ajax/add" method="post">
			<table>
				<tr>
					<td align="right">用户名</td>
					<td><input type="text" name="u.username" id="username" placeholder="用户名" maxlength="20" class="animate"/></td>
				</tr>
				<tr>
					<td align="right">密码</td>
					<td><input type="password" name="u.password" id="password" placeholder="密码" class="animate"/></td>
				</tr>
				<tr>
					<td colspan="2" align="center"><a href="javascript:void(0)" id="addbtn" class="animate">添加</a></td>
				</tr>
				<tr><td colspan="2"><a href="${webRoot}/list">查看列表</a></td></tr>
			</table>
		</form>
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
		
		$('#addbtn').on('click',function(){
			var username=$('#username'),password=$('#password');
			if(username.val()==''){
				$.alert('请输入用户名！',function(){username.focus()});
			}else if(password.val()==''){
				$.alert('请输入密码！',function(){password.focus()});
			}else{
				var form=$('#myform');
				$.post(form.attr('action'),form.serialize(),function(json){
					$.alert(json.msg,function(){
						if(!json.err){
							location.href='${webRoot}/list';
						}
					})
				},'json');
			}
		});
	});
	</script>
</html>
