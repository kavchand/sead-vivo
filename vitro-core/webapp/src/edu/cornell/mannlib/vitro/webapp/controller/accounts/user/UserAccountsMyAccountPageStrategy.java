/*
Copyright (c) 2013, Cornell University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of Cornell University nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package edu.cornell.mannlib.vitro.webapp.controller.accounts.user;

import static javax.mail.Message.RecipientType.TO;

import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsPage;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.email.FreemarkerEmailFactory;
import edu.cornell.mannlib.vitro.webapp.email.FreemarkerEmailMessage;

/**
 * Handle the variant details of the MyAccounts page
 */
public abstract class UserAccountsMyAccountPageStrategy extends
		UserAccountsPage {

	private static final String CONFIRM_CHANGE = "confirmChange";
	private static final String CONFIRM_EMAIL_SENT = "confirmEmailSent";

	protected final UserAccountsMyAccountPage page;

	public static UserAccountsMyAccountPageStrategy getInstance(
			VitroRequest vreq, UserAccountsMyAccountPage page,
			boolean externalAuth) {
		if (externalAuth) {
			return new ExternalAuthStrategy(vreq, page);
		} else {
			return new InternalAuthStrategy(vreq, page);
		}
	}

	protected UserAccountsMyAccountPageStrategy(VitroRequest vreq,
			UserAccountsMyAccountPage page) {
		super(vreq);
		this.page = page;
	}

	public abstract void parseAdditionalParameters();

	public abstract String additionalValidations();

	public abstract void addMoreBodyValues(Map<String, Object> body);

	public abstract void setAdditionalProperties(UserAccount userAccount);

	public abstract void notifyUser();

	public abstract String getConfirmationCode();

	// ----------------------------------------------------------------------
	// Strategy to use if the account used External Authentication
	// ----------------------------------------------------------------------

	private static class ExternalAuthStrategy extends
			UserAccountsMyAccountPageStrategy {

		ExternalAuthStrategy(VitroRequest vreq, UserAccountsMyAccountPage page) {
			super(vreq, page);
		}

		@Override
		public void parseAdditionalParameters() {
			// No additional parameters
		}

		@Override
		public String additionalValidations() {
			// No additional validations
			return "";
		}

		@Override
		public void addMoreBodyValues(Map<String, Object> body) {
			body.put("externalAuth", Boolean.TRUE);
		}

		@Override
		public void setAdditionalProperties(UserAccount userAccount) {
			// No additional properties.
		}

		@Override
		public void notifyUser() {
			// No notification beyond the screen message.
		}

		@Override
		public String getConfirmationCode() {
			return CONFIRM_CHANGE;
		}
	}

	// ----------------------------------------------------------------------
	// Strategy to use if the account used Internal Authentication
	// ----------------------------------------------------------------------

	private static class InternalAuthStrategy extends
			UserAccountsMyAccountPageStrategy {
		private static final String PARAMETER_NEW_PASSWORD = "newPassword";
		private static final String PARAMETER_CONFIRM_PASSWORD = "confirmPassword";

		private static final String ERROR_WRONG_PASSWORD_LENGTH = "errorPasswordIsWrongLength";
		private static final String ERROR_PASSWORDS_DONT_MATCH = "errorPasswordsDontMatch";

		private static final String EMAIL_TEMPLATE = "userAccounts-confirmEmailChangedEmail.ftl";

		private final String originalEmail;

		private String newPassword;
		private String confirmPassword;
		private boolean emailSent;

		InternalAuthStrategy(VitroRequest vreq, UserAccountsMyAccountPage page) {
			super(vreq, page);
			originalEmail = page.getUserAccount().getEmailAddress();
		}

		@Override
		public void parseAdditionalParameters() {
			newPassword = getStringParameter(PARAMETER_NEW_PASSWORD, "");
			confirmPassword = getStringParameter(PARAMETER_CONFIRM_PASSWORD, "");
		}

		@Override
		public String additionalValidations() {
			if (!page.isExternalAuthOnly()) {
				if (newPassword.isEmpty() && confirmPassword.isEmpty()) {
					return "";
				} else if (!newPassword.equals(confirmPassword)) {
					return ERROR_PASSWORDS_DONT_MATCH;
				} else if (!checkPasswordLength(newPassword)) {
					return ERROR_WRONG_PASSWORD_LENGTH;
				} else {
					return "";
				}
			} else {
				return "";
			}
		}

		@Override
		public void addMoreBodyValues(Map<String, Object> body) {
			body.put("newPassword", newPassword);
			body.put("confirmPassword", confirmPassword);
			body.put("minimumLength", UserAccount.MIN_PASSWORD_LENGTH);
			body.put("maximumLength", UserAccount.MAX_PASSWORD_LENGTH);
		}

		@Override
		public void setAdditionalProperties(UserAccount userAccount) {
			if (!newPassword.isEmpty() && !page.isExternalAuthOnly()) {
				userAccount.setMd5Password(Authenticator
						.applyMd5Encoding(newPassword));
				userAccount.setPasswordChangeRequired(false);
				userAccount.setPasswordLinkExpires(0L);
			}
		}

		@Override
		public void notifyUser() {
			if (!isEmailEnabled()) {
				return;
			}
			if (!emailHasChanged()) {
				return;
			}

			Map<String, Object> body = new HashMap<String, Object>();
			body.put("userAccount", page.getUserAccount());
			body.put("siteName", getSiteName());

			FreemarkerEmailMessage email = FreemarkerEmailFactory
					.createNewMessage(vreq);
			email.addRecipient(TO, page.getUserAccount().getEmailAddress());
			email.setSubject("Your VIVO email account has been changed.");
			email.setTemplate(EMAIL_TEMPLATE);
			email.setBodyMap(body);
			email.processTemplate();
			email.send();

			emailSent = true;
		}

		private boolean emailHasChanged() {
			return !page.getUserAccount().getEmailAddress()
					.equals(originalEmail);
		}

		@Override
		public String getConfirmationCode() {
			return emailSent ? CONFIRM_EMAIL_SENT : CONFIRM_CHANGE;
		}

	}

}
