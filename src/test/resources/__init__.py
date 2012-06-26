def test_handle(request):
    reqStr = request.read()
    line = "echo :" +str(type(request))+":"+reqStr
    request.write(line)
