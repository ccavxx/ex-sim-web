<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width" />
    <meta http-equiv="x-ua-compatible" content="IE=8">
    <title>远程桌面</title>
 	<link href="/css/bootstrap.css" rel="stylesheet" media="screen">
    <link href="/js/jquery-easyui/themes/bootstrap/easyui.css" rel="stylesheet" type="text/css">
    <link href="/css/system.css" rel="stylesheet" type="text/css">
   
    <script src="/js/global/jquery-1.8.3.js"></script>
    <script src="/js/global/bootstrap.js"></script>
  	<script src="/js/jquery-easyui/jquery.easyui.js"></script>
    
	<script type="text/javascript">
           function RdpConnect(server,userName,password,port) {
                if(!port){
                	port = 3389 ;
                }
                if (!MsRdpClient.Connected) {
                    try {
                        $("#connectArea").css("display","block"); //显示远程桌面div
                        MsRdpClient.Server = server; //设置远程桌面IP地址
                        try {
                            MsRdpClient.AdvancedSettings2.RedirectDrives = false;
                            MsRdpClient.AdvancedSettings2.RedirectPrinters = false;
                            MsRdpClient.AdvancedSettings2.RedirectPrinters = false;
                            MsRdpClient.AdvancedSettings2.RedirectClipboard = true;
                            MsRdpClient.AdvancedSettings2.RedirectSmartCards = false;
                        } catch (ex) {
                        };

                        MsRdpClient.Domain = "myDomain";//域
                        MsRdpClient.UserName = userName;
                        //MsRdpClient.Password = password;
                        //MsRdpClient.SecuredSettings.StartProgram = StartProgram
                        MsRdpClient.AdvancedSettings2.ClearTextPassword = password; //密码
                        MsRdpClient.AdvancedSettings2.RDPPort = port; //端口
                        ColorDepthDefault();
                        ScreenDefault();
                        try {
                            //如果不支持，继续下面操作
                            MsRdpClient.AdvancedSettings7.EnableCredSspSupport = true;
                            MsRdpClient.AdvancedSettings5.AuthenticationLevel = 2;
                        } catch (ex) {
                        	alert(ex.message) ;
                        } finally {       
                            MsRdpClient.Connect();  //连接远程桌面
                        }
                    } catch (ex) {
                        alert("发生错误：" + ex.message + "请尝试刷新页面重新连接。");
                    };
                } else {
                    alert("已连接！");
                };
            };
            //var logoff = false; //存储是否正常注销
            //全屏
            function FullScreen() {
                if (MsRdpClient.Connected) {
                    MsRdpClient.FullScreen = 1;
                };
            };
            function ScreenDefault() {
                MsRdpClient.Width = ${empty param.width ? 1024 : param.width}; //设置远程桌面 显示区域的宽和高
                MsRdpClient.Height = ${empty param.width ? 768 : param.height};
                MsRdpClient.DesktopWidth = ${empty param.width ? 1024 : param.width};  //设置 远程桌面的宽和高
                MsRdpClient.DesktopHeight = ${empty param.height ? 1024 : param.height};
            };
            //色彩度，默认32位
            function ColorDepthDefault() {
                MsRdpClient.ColorDepth = 32;
            };
			//关闭当前页面
			function closeWindow() {
				window.opener = null;
				window.open("", "_self");
				window.close();
			};
			function enterHandler(event,callback){
				var event = event || window.event || arguments.callee.caller.arguments[0] ;
				var keyCode = event.keyCode ? event.keyCode : event.which ? event.which : event.charCode;  
			    if (keyCode == 13 && callback) {  
			        callback.call() ;
			    }   
			};
			function login(){
				RdpConnect($("#ip").val(), $("#username").val(), $("#password").val(), $("#port").val()) ;
			}
	</script>    
</head>
<body style="padding: 0;margin: 0;">
	<div class="easyui-layout sim" fit="true">
		<div id="loginInfo" data-options="region:'north',title:'远程桌面到${param.ip}',height:68">
			<form id="loginForm" class="form-inline" style="padding: 5px;margin: 0px;" onsubmit="return false" onkeydown="enterHandler(event, login)">
				<input id="ip" type="hidden" value="${param.ip}"/>
				<span>用户名：</span><input id="username" type="text" class="input-medium" value="${accountName}" autocomplete="off">
				<span>密码：</span><input id="password" type="password" class="input-meidum" value="${accountPassword}" autocomplete="off">
				<span>端口：</span><input id="port" type="text" class="input-mini" value="${empty defaultPort ? '3389' : defaultPort}">
				<button type="button" class="btn" onclick="login();">连接</button>
				<button type="button" class="btn" onclick="FullScreen()">全屏</button>
			</form>
		</div>
		<div data-options="region:'center',fit:true">
		    <div id="connectArea" style="display: none;width: 100%;height: 100%;">
		        <table>
		            <tr>
		                <td>
		                    <object id="MsRdpClient"
		                        classid="CLSID:7584c670-2274-4efb-b00b-d6aaba6d3850"
		                        codebase="msrdp.cab#version=5,2,3790,0" width="${empty param.width ? 1024 : param.width }" height="${empty param.height ? 768 : param.height }">
		                    </object>
		                </td>
		            </tr>
				    <script type="text/javascript">
				        function MsRdpClient::OnDisconnected(disconnectCode) {
				            $("connectArea").css("display","none");
				            //closeWindow();
				        }
				    </script>
		        </table>
		    </div>
		</div>
	</div>
</body>
</html>