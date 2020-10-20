package put.io.selenium.atm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * A controller Servlet of the application.
 * 
 */
@SuppressWarnings("serial")
public class AtmServlet extends HttpServlet {
	private Configuration tmplCfg;

	private boolean testMode;
	
	@Override
	public void init() throws ServletException {
		this.testMode = Boolean.parseBoolean(getServletConfig().getInitParameter("testMode"));
		tmplCfg = new Configuration();
		ClassTemplateLoader loader = new ClassTemplateLoader(this.getClass(),
				"/pages");
		tmplCfg.setTemplateLoader(loader);
		tmplCfg.setDefaultEncoding("UTF-8");
		tmplCfg.setObjectWrapper(new DefaultObjectWrapper());
		super.init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);

		final String tmplName;
		Map<String, Object> data = new HashMap<String, Object>();

		HttpSession sess = request.getSession(true);
		AtmCardInfo atmCard = (AtmCardInfo) sess.getAttribute("atm_card_info");
		if (atmCard == null) {
			atmCard = new AtmCardInfo();
			tmplName = "init";
		} else {
			final String action = (String) request.getServletPath().replaceFirst(
					"^/", "");

			// for tests only!
			if (testMode && "test_only_reset_all".equals(action)) {
				atmCard = new AtmCardInfo();
				tmplName = "init";
			} else {
				tmplName = serveRequest(action, request, response, atmCard,
						data);
			}
			data.put("pin_tries_left", atmCard.pinTriesLeft);
		}
		sess.setAttribute("atm_card_info", atmCard);

		Template tmpl = tmplCfg.getTemplate(tmplName);
		try {
			tmpl.process(data, response.getWriter());
		} catch (TemplateException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Controls the flow of the application.
	 * 
	 * @param action
	 *            is the name of the action that was performed by a user.
	 * @param request
	 *            is a request object send in the HTTP request.
	 * @param response
	 *            is a HTTP response that will be sent to the user.
	 * @param atmCard
	 *            is the state of ATM for the user session.
	 * @param data is the model data that will be filled depending on action.
	 * @return a message that will be displayed to the user.
	 */
	private String serveRequest(String action, HttpServletRequest request,
			HttpServletResponse response, AtmCardInfo atmCard,
			Map<String, Object> data) {
		if ("init".equals(action)) {
			return "init";
		} else if ("card_in".equals(action)) {
			if (atmCard.isCardLocked) {
				return "card_locked";
			} else {
				atmCard.cardIn();
				return "enter_pin";
			}
		} else if ("enter_pin".equals(action)) {
			final String pin = (String) request.getParameter("pin");
			if (atmCard.tryPin(pin)) {
				return "choose_op";
			} else {
				if (atmCard.isCardLocked) {
					return "wrong_pin_card_locked";
				} else {
					return "wrong_pin";
				}
			}
		} else if ("card_out".equals(action)) {
			atmCard.cardOut();
			return "init";
		} else if ("op_withdraw".equals(action)) {
			return "enter_withdrawal_amount";
		} else if ("withdraw_amount".equals(action)) {
			final String amount = (String) request.getParameter("amount");
			String error = atmCard.tryWithdraw(amount);
			if (error != null) {
				data.put("error_msg", error);
				return "enter_withdrawal_amount";
			} else {
				return "ask_take_card";
			}
		} else if ("withdraw_take_card".equals(action)) {
			return "ask_take_money";
		} else if ("withdraw_take_money".equals(action)) {
			return "thank_you";
		} else {
			return "init";
		}
	}
}
