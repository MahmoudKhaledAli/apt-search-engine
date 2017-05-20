<%-- 
    Document   : test
    Created on : May 7, 2017, 10:42:38 PM
    Author     : Mahmoud
--%>

<%@page import="java.util.List"%>
<%@page import="struct.Result"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <style>
            input::-webkit-calendar-picker-indicator {
                display: none;
            }
            .navbar {
                height: 70px;
            }
            a:link {
                text-decoration: none!important;
                cursor: pointer;
            }
            div.results {
                width: 800px;
                padding-left: 150px;
                font-family: "Helvetica Neue",Helvetica,Arial,sans-serif;
            }
            .bar {
                padding-left: 150px;
            }
            .num {
                font-size: 20px;
            }
            .link {
                width: 500px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
                color: green;
            }
        </style>
        <script>
            var page;
            function getSuggestions() {
                var text = $("#searchbox").val();
                $.get("/SearchEngine/Suggestions", {queryso: text}, function (data) {
                    for (var i = 0; i < data.suggestions.length; i++) {
                        console.log(text);
                        console.log(data.suggestions[i]);
                        $("#option" + (i + 1)).show();
                        $("#option" + (i + 1)).val(data.suggestions[i]);
                        $("#option" + (i + 1)).text(data.suggestions[i]);
                    }
                    for (var i = data.suggestions.length + 1; i < 6; i++) {
                        console.log("#option" + i);
                        $("#option" + i).hide();
                        $("#option" + i).text("");
                        $("#option" + i).val("");
                    }
                });
            }
        </script>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <% List<Result> results = (List<Result>) request.getAttribute("results"); %>
        <% String query = (String) request.getAttribute("query");%>
        <title><%= query%> - 7oodle Search</title>
        <script>
            function nextPage() {
                page++;
                for (var i = 0; i < <%=results.size()%> / 10 + 1; i++) {
                    $("#link" + i).hide();
                }
                $("#link" + (page - 1)).show();
                $("#link" + (page - 2)).show();
                $("#link" + (page)).show();
                if (page === 1) {
                    $("#pagprev").hide();
                } else {
                    $("#pagprev").show();
                }
                if (page === <%=results.size() / 10%> + 1) {
                    $("#pagnext").hide();
                    $("#link" + (page - 3)).show();
                } else {
                    $("#pagnext").show();
                }
                for (var i = 0; i < <%=results.size() / 10%> + 1; i++) {
                    $("#link" + i).removeClass('active');
                }
                $("#link" + (page - 1)).addClass('active');
                for (var i = 0; i < <%=results.size()%>; i++) {
                    var x = document.getElementById(i);
                    x.style.display = 'none';
                }
                for (var i = (page - 1) * 10; i < (page - 1) * 10 + 10 && i < <%=results.size()%>; i++) {
                    var x = document.getElementById(i);
                    x.style.display = 'block';
                }
            }
            function prevPage() {
                page--;
                for (var i = 0; i < <%=results.size()%> / 10 + 1; i++) {
                    $("#link" + i).hide();
                }
                $("#link" + (page - 1)).show();
                $("#link" + (page - 2)).show();
                $("#link" + (page)).show();
                if (page === 1) {
                    $("#pagprev").hide();
                    $("#link" + (page + 1)).show();
                } else {
                    $("#pagprev").show();
                }
                if (page === <%=results.size() / 10%> + 1) {
                    $("#pagnext").hide();
                } else {
                    $("#pagnext").show();
                }
                for (var i = 0; i < <%=results.size() / 10%> + 1; i++) {
                    $("#link" + i).removeClass('active');
                }
                $("#link" + (page - 1)).addClass('active');
                for (var i = 0; i < <%=results.size()%>; i++) {
                    var x = document.getElementById(i);
                    x.style.display = 'none';
                }
                for (var i = (page - 1) * 10; i < (page - 1) * 10 + 10 && i < <%=results.size()%>; i++) {
                    var x = document.getElementById(i);
                    x.style.display = 'block';
                }
            }
            function showHide(j) {
                page = j / 10 + 1;
                for (var i = 0; i < <%=results.size()%> / 10 + 1; i++) {
                    $("#link" + i).hide();
                }
                if (j === <%=results.size()%> / 10 + 1) {
                    $("#link" + (j / 10)).show();
                    $("#link" + ((j / 10) - 1)).show();
                    $("#link" + ((j / 10) - 2)).show();
                } else if (j === 0) {
                    $("#link0").show();
                    $("#link1").show();
                    $("#link2").show();
                } else {
                    $("#link" + (j / 10)).show();
                    $("#link" + ((j / 10) - 1)).show();
                    $("#link" + ((j / 10) + 1)).show();
                }
                if (page === 1) {
                    $("#pagprev").hide();
                } else {
                    $("#pagprev").show();
                }
                if (page === <%=results.size() / 10%> + 1) {
                    $("#pagnext").hide();
                } else {
                    $("#pagnext").show();
                }
                for (var i = 0; i < <%=results.size() / 10%> + 1; i++) {
                    $("#link" + i).removeClass('active');
                }
                $("#link" + j / 10).addClass('active');
                for (var i = 0; i < <%=results.size()%>; i++) {
                    var x = document.getElementById(i);
                    x.style.display = 'none';
                }
                for (var i = j; i < j + 10 && i < <%=results.size()%>; i++) {
                    var x = document.getElementById(i);
                    x.style.display = 'block';
                }
            }
        </script>
        <script>
            $(document).ready(function () {
                showHide(0);
                page = 1;
                $("#option1").hide();
                $("#option2").hide();
                $("#option3").hide();
                $("#option4").hide();
                $("#option5").hide();
                for (var i = 0; i < <%=results.size()%> / 10 + 1; i++) {
                    $("#link" + i).hide();
                }
                $("#link0").show();
                $("#link1").show();
                $("#link2").show();
            });
        </script>
    </head>
    <body>
        <nav class="navbar navbar-default">
            <div class="container-fluid">
                <div class="navbar-header">
                    <a class="navbar-brand" href="/SearchEngine"><img src="LogoTop.png"
                                                                      alt="Logo" height="44"/>
                    </a>
                </div>
                <form class="navbar-form navbar-left" action="Search" method="POST">
                    <div class="form-group">
                        <input type="text" value='<%= query%>' style="width:500px;height:44px;margin-top:5px;"
                               class="form-control" placeholder="Search" list="suggest"
                               oninput="getSuggestions()"   autocomplete="off"
                               name="query" id="searchbox">
                        <datalist id ="suggest">
                            <option id='option1' value=''>w</option>
                            <option id='option2' value=''>w</option>
                            <option id='option3' value=''>w</option>
                            <option id='option4' value=''>w</option>
                            <option id='option5' value=''>w</option>
                        </datalist>
                    </div>
                    <button type="submit" class="btn btn-default" style="height:44px;margin-top:5px;
                            margin-left:5px;">
                        <span class="glyphicon glyphicon-search"></span> Search
                    </button>
                </form>
            </div>
        </nav>
        <div id="results">
            <% for (int i = 0; i < results.size(); i++) {%>
            <div id="<%= i%>" class="results" align="left">
                <div><a style = "font-size: 20px" href = "<%= results.get(i).url%>"><%= results.get(i).title%></a></div>
                <div class="link" style="font-size: 14px"> <%= results.get(i).url%> </div>
                <div style="font-size: 14px"><%= results.get(i).snippet%></div>
                <br>
            </div>
            <%}%>
        </div>
        <div align='center'>
            <ul class="pagination pagination-lg">
                <li id="pagprev"><a href =" #" onclick="prevPage()">Previous</a></li>
                    <% for (int i = 0; i < (results.size() + 10) / 10; i++) {%>
                <li id="<%= "link" + i%>"><a  href = "#" onclick = "showHide(<%= i * 10%>)"> <%= i + 1%></a></li>
                    <%}%>
                <li id="pagnext"><a href =" #" onclick="nextPage()">Next</a></li>
            </ul>
        </div>
    </body>
</html>
