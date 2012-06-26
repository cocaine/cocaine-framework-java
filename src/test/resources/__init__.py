from cocaine.decorators import native

@native
def test_handle(meta, request):
    return request