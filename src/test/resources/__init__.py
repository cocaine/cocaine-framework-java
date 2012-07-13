import time
def test_handle(request):
    aa = request.read()
    request.write("python replies: "+str(request)+" "+aa)

def failing_handle(request):
    raise Exception('spamm', 'egs')

def test_handle_chunked(request):
    aa = request.read()
    request.write("first chunk "+aa)
    time.sleep(0.01)
    request.write("second_chunk")
    time.sleep(0.01)
    request.write("third_chunk")
    for i in range(10):
        request.write(str(i)+" chunk")
        time.sleep(0.01)
