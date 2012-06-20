#ifndef COCAINE_DEALER_JAVA_BINDING_RESPONSE_HOLDER_OBJECT
#define COCAINE_DEALER_JAVA_BINDING_RESPONSE_HOLDER_OBJECT

#include <boost/shared_ptr.hpp>
#include <cocaine/dealer/response.hpp>

namespace cocaine { namespace dealer { namespace java {

class response_holder_t {
	public:
		explicit response_holder_t(const boost::shared_ptr<response_t>& response_):
			m_response(response_)
		{ }
		response_t* get () {
			return m_response.get();
		}
	private:
		boost::shared_ptr<response_t> m_response;
};

}}}
#endif
