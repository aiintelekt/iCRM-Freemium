package org.fio.crm.common.login;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.transaction.Transaction;

import org.ofbiz.base.crypto.HashCrypt;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.common.authentication.AuthHelper;
import org.ofbiz.common.authentication.api.AuthenticatorException;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.ModelEntity;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityQuery;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.util.EntityUtilProperties;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.webapp.control.LoginWorker;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;

public class LoginServices {

	public static final String module = LoginServices.class.getName();
	public static final String resource = "SecurityextUiLabels";

	/**
	 * Login service to authenticate username and password
	 * 
	 * @return Map of results including (userLogin) GenericValue object
	 */
	public static Map<String, Object> ldapUserLoginService(DispatchContext ctx, Map<String, ?> context) {
		LocalDispatcher dispatcher = ctx.getDispatcher();
		Locale locale = (Locale) context.get("locale");
		Delegator delegator = ctx.getDelegator();

		// load the external auth modules -- note: this will only run once and cache the
		// objects
		if (!AuthHelper.authenticatorsLoaded()) {
			AuthHelper.loadAuthenticators(dispatcher);
		}

		// Authenticate to LDAP if configured to do so
		// TODO: this should be moved to using the NEW Authenticator API
		/*
		 * if ("true".equals(EntityUtilProperties.getPropertyValue("security",
		 * "security.ldap.enable", delegator))) { if
		 * (!LdapAuthenticationServices.userLogin(ctx, context)) { String errMsg =
		 * UtilProperties.getMessage(resource,
		 * "loginservices.ldap_authentication_failed", locale); if
		 * ("true".equals(EntityUtilProperties.getPropertyValue("security",
		 * "security.ldap.fail.login", delegator))) { return
		 * ServiceUtil.returnError(errMsg); } else { Debug.logInfo(errMsg, module); } }
		 * }
		 */

		Map<String, Object> result = new LinkedHashMap<String, Object>();
		boolean useEncryption = "true"
				.equals(EntityUtilProperties.getPropertyValue("security", "password.encrypt", delegator));

		// if isServiceAuth is not specified, default to not a service auth
		boolean isServiceAuth = context.get("isServiceAuth") != null
				&& ((Boolean) context.get("isServiceAuth")).booleanValue();

		String username = (String) context.get("login.username");
		if (UtilValidate.isEmpty(username))
			username = (String) context.get("username");
		String password = (String) context.get("login.password");
		if (UtilValidate.isEmpty(password))
			password = (String) context.get("password");

		// get the visitId for the history entity
		String visitId = (String) context.get("visitId");

		String errMsg = "";
		if (UtilValidate.isEmpty(username) || username.length() <= 0) {//parasoft-suppress BD.PB.CC "not an issue"
			errMsg = UtilProperties.getMessage(resource, "loginservices.username_missing", locale);
		} else if (UtilValidate.isEmpty(password) || password.length() <= 0) {
			errMsg = UtilProperties.getMessage(resource, "loginservices.password_missing", locale);
		} else {
			if ("true".equalsIgnoreCase(
					EntityUtilProperties.getPropertyValue("security", "username.lowercase", delegator))) {
				username = username.toLowerCase();
			}
			if ("true".equalsIgnoreCase(
					EntityUtilProperties.getPropertyValue("security", "password.lowercase", delegator))) {
				password = password.toLowerCase();
			}
			
			GenericValue userLogin = null;
			
			// LDAP Integration Start
			String ldapValidation = EntityUtilProperties.getPropertyValue("security", "security.ldap.enable", delegator);
			if ("true".equals(ldapValidation) && (UtilValidate.isNotEmpty(username) && !username.equals("admin"))) {

				String ldapDomain = EntityUtilProperties.getPropertyValue("crm", "ldapDomain1", delegator);
				String ldapEmailDomain = EntityUtilProperties.getPropertyValue("crm", "ldapEmailDomain1", delegator);
				String ldapUrl = EntityUtilProperties.getPropertyValue("crm", "ldapUrl1", delegator);

				Map<String, Object> inputFields = UtilMisc.<String, Object>toMap("ldapUserName", username,
						"ldapPassword", password, "ldapDomain", ldapDomain, "ldapEmailDomain", ldapEmailDomain,
						"ldapUrl", ldapUrl);
				Map<String, Object> returnMap = new HashMap<String, Object>();
				try {
					returnMap = dispatcher.runSync("getLDAPSecurityGroups", UtilMisc.toMap("inputFields", inputFields));

					if (returnMap != null && "ERROR".equals(returnMap.get("status"))) {

						ldapDomain = EntityUtilProperties.getPropertyValue("crm", "ldapDomain3", delegator);
						ldapEmailDomain = EntityUtilProperties.getPropertyValue("crm", "ldapEmailDomain3", delegator);
						ldapUrl = EntityUtilProperties.getPropertyValue("crm", "ldapUrl3", delegator);

						inputFields = UtilMisc.<String, Object>toMap("ldapUserName", username, "ldapPassword", password,
								"ldapDomain", ldapDomain, "ldapEmailDomain", ldapEmailDomain, "ldapUrl", ldapUrl);
						returnMap = dispatcher.runSync("getLDAPSecurityGroups",
								UtilMisc.toMap("inputFields", inputFields));

						if (returnMap != null && "ERROR".equals(returnMap.get("status"))) {
							result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_FAIL);
							result.put(ModelService.ERROR_MESSAGE, UtilProperties.getMessage("crmUiLabels", "invalidLDAPCredentials", locale));
							return result;
						}
					}
					// Authentication auth=(Authentication)returnMap.get("authentication");
					if ("SUCCESS".equals(returnMap.get("status"))) {
						Debug.logInfo("[LoginServices.userLogin] : LDAP Authentication success for: "+username, module);
						try {
							List<String> securityGroups = new LinkedList<String>();
							List<String> roleTypeIds = new LinkedList<String>();
							GenericValue LDAPuserLogin = EntityQuery.use(delegator).from("UserLogin").where("userLoginId", username)
									.cache(isServiceAuth).queryOne();
							if(LDAPuserLogin != null && LDAPuserLogin.size() > 0) {
								String partyId = LDAPuserLogin.getString("partyId");
								if(UtilValidate.isNotEmpty(partyId)) {
									try {
										addCalendarOwnerRoleType(delegator, partyId);
									    dispatcher.runSync("createSecurityGroupFromPosition", UtilMisc.toMap("partyId", partyId, "userName", username));
									} catch (Exception e) {
										Debug.logError("Exception in createSecurityGroupFromPosition service "+e.getMessage(), module);
									}
								}
								List<GenericValue> LDAPuserLoginSecurityGroup = EntityQuery.use(delegator).from("UserLoginSecurityGroup").where("userLoginId", username)
										.filterByDate().queryList();
								if(LDAPuserLoginSecurityGroup != null && LDAPuserLoginSecurityGroup.size() > 0) {
									for(GenericValue LDAPuserLoginSecurityGroupGV : LDAPuserLoginSecurityGroup) {
										securityGroups.add(LDAPuserLoginSecurityGroupGV.getString("groupId"));
									}
									result.put("securityGroups",securityGroups);
									Debug.log("List of security groups " + securityGroups);
								} else {
									result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_FAIL);
									result.put(ModelService.ERROR_MESSAGE, UtilProperties.getMessage("crmUiLabels", "securityGroupsNotFound", locale));
									return result;
								}
								if(UtilValidate.isNotEmpty(partyId)) {
									List<GenericValue> LDAPPartyRole = EntityQuery.use(delegator).from("PartyRole").where("partyId", partyId)
									    .queryList();
									if(LDAPPartyRole != null && LDAPPartyRole.size() > 0) {
										for(GenericValue LDAPPartyRoleGV : LDAPPartyRole) {
											roleTypeIds.add(LDAPPartyRoleGV.getString("roleTypeId"));
										}
										result.put("roleTypeIds",roleTypeIds);
										Debug.log("List of role type ids " + roleTypeIds);
									} else {
										result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_FAIL);
										result.put(ModelService.ERROR_MESSAGE, UtilProperties.getMessage("crmUiLabels", "rolesNotFound", locale));
										return result;
									}
								} else {
									result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_FAIL);
									result.put(ModelService.ERROR_MESSAGE, UtilProperties.getMessage("crmUiLabels", "rolesNotFound", locale));
									return result;
								}
								
								// we might change & store this userLogin, so we should clone it here to get a
								// mutable copy
								LDAPuserLogin = GenericValue.create(LDAPuserLogin);
								
	                            TransactionUtil.begin();
								
	                            LDAPuserLogin.set("enabled", "Y");
	                            LDAPuserLogin.set("hasLoggedOut", "N");
	                            LDAPuserLogin.set("userLoginId", username);
	                            LDAPuserLogin.store();
								
								result.put("userLogin", LDAPuserLogin);
								
								Map<String, Object> userLoginHistory = UtilMisc.toMap("userLoginId", username,
										"visitId", visitId, "fromDate", UtilDateTime.nowTimestamp(),
										"successfulLogin", "Y");

								ModelEntity modelUserLogin = LDAPuserLogin.getModelEntity();
								if (modelUserLogin.isField("partyId")) {
									userLoginHistory.put("partyId", LDAPuserLogin.get("partyId"));
								}

								delegator.create("UserLoginHistory", userLoginHistory);
								TransactionUtil.commit();
								
								result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
								return result;
							}
							
							} catch(Exception e) {
								Debug.log("Exception" + e.getMessage());
								result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_FAIL);
								result.put(ModelService.ERROR_MESSAGE, UtilProperties.getMessage("crmUiLabels", "invalidLDAPCredentials", locale));
								return result;
							}
						
					} else {
						result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_FAIL);
						result.put(ModelService.ERROR_MESSAGE, UtilProperties.getMessage("crmUiLabels", "invalidLDAPCredentials", locale));
						return result;
					}

				} catch (GenericServiceException e1) {
					Debug.log("Exception in getLDAPSecurityGroups");
					result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_FAIL);
					result.put(ModelService.ERROR_MESSAGE, e1.getMessage());
					return result;
				}
				
			}
			// LDAP Integration End

			boolean repeat = true;
			// starts at zero but it incremented at the beginning so in the first pass
			// passNumber will be 1
			int passNumber = 0;

			while (repeat) {
				repeat = false;
				// pass number is incremented here because there are continues in this loop so
				// it may never get to the end
				passNumber++;

				try {
					// only get userLogin from cache for service calls; for web and other manual
					// logins there is less time sensitivity
					userLogin = EntityQuery.use(delegator).from("UserLogin").where("userLoginId", username)
							.cache(isServiceAuth).queryOne();
				} catch (GenericEntityException e) {
					Debug.logWarning(e, "", module);
				}

				// see if any external auth modules want to sync the user info
				if (userLogin == null) {
					try {
						AuthHelper.syncUser(username);
					} catch (AuthenticatorException e) {
						Debug.logWarning(e, module);
					}

					// check the user login object again
					try {
						userLogin = EntityQuery.use(delegator).from("UserLogin").where("userLoginId", username)
								.cache(isServiceAuth).queryOne();
					} catch (GenericEntityException e) {
						Debug.logWarning(e, "", module);
					}
				}

				if (userLogin != null) {
					String ldmStr = EntityUtilProperties.getPropertyValue("security", "login.disable.minutes",
							delegator);
					long loginDisableMinutes = 30;

					try {
						loginDisableMinutes = Long.parseLong(ldmStr);
					} catch (Exception e) {
						loginDisableMinutes = 30;
						Debug.logWarning(
								"Could not parse login.disable.minutes from security.properties, using default of 30",
								module);
					}

					Timestamp disabledDateTime = userLogin.getTimestamp("disabledDateTime");
					Timestamp reEnableTime = null;

					if (loginDisableMinutes > 0 && disabledDateTime != null) {
						reEnableTime = new Timestamp(disabledDateTime.getTime() + loginDisableMinutes * 60000);
					}

					boolean doStore = true;
					// we might change & store this userLogin, so we should clone it here to get a
					// mutable copy
					userLogin = GenericValue.create(userLogin);

					// get the is system flag -- system accounts can only be used for service
					// authentication
					boolean isSystem = (isServiceAuth && userLogin.get("isSystem") != null)
							? "Y".equalsIgnoreCase(userLogin.getString("isSystem"))
							: false;

					// grab the hasLoggedOut flag
					boolean hasLoggedOut = userLogin.get("hasLoggedOut") != null
							? "Y".equalsIgnoreCase(userLogin.getString("hasLoggedOut"))
							: false;

					if (UtilValidate.isEmpty(userLogin.getString("enabled"))
							|| "Y".equals(userLogin.getString("enabled"))
							|| (reEnableTime != null && reEnableTime.before(UtilDateTime.nowTimestamp()))
							|| (isSystem)) {

						String successfulLogin;

						if (!isSystem) {
							userLogin.set("enabled", "Y");
						}

						// attempt to authenticate with Authenticator class(es)
						boolean authFatalError = false;
						boolean externalAuth = false;
						try {
							externalAuth = AuthHelper.authenticate(username, password, isServiceAuth);
						} catch (AuthenticatorException e) {
							// fatal error -- or single authenticator found -- fail now
							Debug.logWarning(e, module);
							authFatalError = true;

						}
						// if the password.accept.encrypted.and.plain property in security is set to
						// true allow plain or encrypted passwords
						// if this is a system account don't bother checking the passwords
						// if externalAuth passed; this is run as well
						if ((!authFatalError && externalAuth)
								|| checkPassword(userLogin.getString("currentPassword"), useEncryption, password)) {
							Debug.logVerbose("[LoginServices.userLogin] : Password Matched", module);

							// update the hasLoggedOut flag
							if (hasLoggedOut) {
								userLogin.set("hasLoggedOut", "N");
							}

							// reset failed login count if necessry
							Long currentFailedLogins = userLogin.getLong("successiveFailedLogins");
							if (currentFailedLogins != null && currentFailedLogins.longValue() > 0) {
								userLogin.set("successiveFailedLogins", Long.valueOf(0));
							} else if (!hasLoggedOut) {
								// successful login & no loggout flag, no need to change anything, so don't do
								// the store
								doStore = false;
							}

							successfulLogin = "Y";

							if (!isServiceAuth) {
								// get the UserLoginSession if this is not a service auth
								Map<?, ?> userLoginSessionMap = LoginWorker.getUserLoginSession(userLogin);

								// return the UserLoginSession Map
								if (userLoginSessionMap != null) {
									result.put("userLoginSession", userLoginSessionMap);
								}
							}
							if(UtilValidate.isNotEmpty(username) && !"admin".equals(username) && UtilValidate.isNotEmpty(userLogin.getString("partyId"))) {
							    try {
							    	addCalendarOwnerRoleType(delegator, userLogin.getString("partyId"));
								    dispatcher.runSync("createSecurityGroupFromPosition", UtilMisc.toMap("partyId", userLogin.getString("partyId"), "userName", username));
							    } catch(Exception e ) {
							    	Debug.logError("Exception in createSecurityGroupFromPosition service "+e.getMessage(), module);
							    }
							}
							result.put("userLogin", userLogin);
							result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
						} else {
							// Debug.logInfo("Entered password [" + encodedPassword + "], Entered password
							// OldFunnyHexEncode [" + encodedPasswordOldFunnyHexEncode + "], db password ["
							// + userLogin.getString("currentPassword") + "]", module);

							// password is incorrect, but this may be the result of a stale cache entry,
							// so lets clear the cache and try again if this is the first pass
							// but only if authFatalError is not true; this would mean the single
							// authenticator failed
							if (!authFatalError && isServiceAuth && passNumber <= 1) {
								delegator.clearCacheLine("UserLogin", UtilMisc.toMap("userLoginId", username));
								repeat = true;
								continue;
							}

							Debug.logInfo("[LoginServices.userLogin] : Password Incorrect", module);
							// password invalid...
							errMsg = UtilProperties.getMessage(resource, "loginservices.password_incorrect", locale);

							// increment failed login count
							Long currentFailedLogins = userLogin.getLong("successiveFailedLogins");

							if (currentFailedLogins == null) {
								currentFailedLogins = Long.valueOf(1);
							} else {
								currentFailedLogins = Long.valueOf(currentFailedLogins.longValue() + 1);
							}
							userLogin.set("successiveFailedLogins", currentFailedLogins);

							// if failed logins over amount in properties file, disable account
							String mflStr = EntityUtilProperties.getPropertyValue("security", "max.failed.logins",
									delegator);
							long maxFailedLogins = 3;
							try {
								maxFailedLogins = Long.parseLong(mflStr);
							} catch (Exception e) {
								maxFailedLogins = 3;
								Debug.logWarning(
										"Could not parse max.failed.logins from security.properties, using default of 3",
										module);
							}

							if (maxFailedLogins > 0 && currentFailedLogins.longValue() >= maxFailedLogins) {
								userLogin.set("enabled", "N");
								userLogin.set("disabledDateTime", UtilDateTime.nowTimestamp());
							}

							successfulLogin = "N";
						}

						// this section is being done in its own transaction rather than in the
						// current/existing transaction because we may return error and we don't
						// want that to stop this from getting stored
						Transaction parentTx = null;
						boolean beganTransaction = false;

						try {
							try {
								parentTx = TransactionUtil.suspend();
							} catch (GenericTransactionException e) {
								Debug.logError(e, "Could not suspend transaction: " + e.getMessage(), module);
							}

							try {
								beganTransaction = TransactionUtil.begin();

								if (doStore) {
									userLogin.store();
								}

								if ("true".equals(EntityUtilProperties.getPropertyValue("security",
										"store.login.history", delegator))) {
									boolean createHistory = true;

									// only save info on service auth if option set to true to do so
									if (isServiceAuth
											&& !"true".equals(EntityUtilProperties.getPropertyValue("security",
													"store.login.history.on.service.auth", delegator))) {
										createHistory = false;
									}

									if (createHistory) {
										Map<String, Object> ulhCreateMap = UtilMisc.toMap("userLoginId", username,
												"visitId", visitId, "fromDate", UtilDateTime.nowTimestamp(),
												"successfulLogin", successfulLogin);

										ModelEntity modelUserLogin = userLogin.getModelEntity();
										if (modelUserLogin.isField("partyId")) {
											ulhCreateMap.put("partyId", userLogin.get("partyId"));
										}

										// ONLY save the password if it was incorrect
										if ("N".equals(successfulLogin)
												&& !"false".equals(EntityUtilProperties.getPropertyValue("security",
														"store.login.history.incorrect.password", delegator))) {
											ulhCreateMap.put("passwordUsed", password);
										}

										// Debug.logInfo(new Exception(), "=================== Creating new
										// UserLoginHistory at " + UtilDateTime.nowTimestamp(), module);
										delegator.create("UserLoginHistory", ulhCreateMap);
									}
								}
							} catch (GenericEntityException e) {
								String geeErrMsg = "Error saving UserLoginHistory";
								if (doStore) {
									geeErrMsg += " and updating login status to reset hasLoggedOut, unsuccessful login count, etc.";
								}
								geeErrMsg += ": " + e.toString();
								try {
									TransactionUtil.rollback(beganTransaction, geeErrMsg, e);
								} catch (GenericTransactionException e2) {
									Debug.logError(e2, "Could not rollback nested transaction: " + e2.getMessage(),
											module);
								}

								// if doStore is true then this error should not be ignored and we shouldn't
								// consider it a successful login if this happens as there is something very
								// wrong lower down that will bite us again later
								if (doStore) {
									return ServiceUtil.returnError(geeErrMsg);
								}
							} finally {
								try {
									TransactionUtil.commit(beganTransaction);
								} catch (GenericTransactionException e) {
									Debug.logError(e, "Could not commit nested transaction: " + e.getMessage(), module);
								}
							}
						} finally {
							// resume/restore parent transaction
							if (parentTx != null) {
								try {
									TransactionUtil.resume(parentTx);
									Debug.logVerbose("Resumed the parent transaction.", module);
								} catch (GenericTransactionException e) {
									Debug.logError(e, "Could not resume parent nested transaction: " + e.getMessage(),
											module);
								}
							}
						}
					} else {
						// account is disabled, but this may be the result of a stale cache entry,
						// so lets clear the cache and try again if this is the first pass
						if (isServiceAuth && passNumber <= 1) {
							delegator.clearCacheLine("UserLogin", UtilMisc.toMap("userLoginId", username));
							repeat = true;
							continue;
						}

						Map<String, Object> messageMap = UtilMisc.<String, Object>toMap("username", username);
						errMsg = UtilProperties.getMessage(resource, "loginservices.account_for_user_login_id_disabled",
								messageMap, locale);
						if (disabledDateTime != null) {
							messageMap = UtilMisc.<String, Object>toMap("disabledDateTime", disabledDateTime);
							errMsg += " " + UtilProperties.getMessage(resource, "loginservices.since_datetime",
									messageMap, locale);
						} else {
							errMsg += ".";
						}

						if (loginDisableMinutes > 0 && reEnableTime != null) {
							messageMap = UtilMisc.<String, Object>toMap("reEnableTime", reEnableTime);
							errMsg += " " + UtilProperties.getMessage(resource, "loginservices.will_be_reenabled",
									messageMap, locale);
						} else {
							errMsg += " " + UtilProperties.getMessage(resource,
									"loginservices.not_scheduled_to_be_reenabled", locale);
						}
					}
				} else {
					// no userLogin object; there may be a non-syncing authenticator
					boolean externalAuth = false;
					try {
						externalAuth = AuthHelper.authenticate(username, password, isServiceAuth);
					} catch (AuthenticatorException e) {
						errMsg = e.getMessage();
						Debug.logError(e, "External Authenticator had fatal exception : " + e.getMessage(), module);
					}
					if (externalAuth) {
						// external auth passed - create a placeholder object for session
						userLogin = delegator.makeValue("UserLogin");
						userLogin.set("userLoginId", username);
						userLogin.set("enabled", "Y");
						userLogin.set("hasLoggedOut", "N");
						result.put("userLogin", userLogin);
						result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
						// TODO: more than this is needed to support 100% external authentication
						// TODO: party + security information is needed; Userlogin will need to be
						// stored
					} else {
						// userLogin record not found, user does not exist
						errMsg = UtilProperties.getMessage(resource, "loginservices.user_not_found", locale);
						Debug.logInfo("[LoginServices.userLogin] Invalid User : '" + username + "'; " + errMsg, module);
					}
				}
			}
		}

		if (errMsg.length() > 0) {
			result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_FAIL);
			result.put(ModelService.ERROR_MESSAGE, errMsg);
		}
		return result;
	}

	private static boolean checkPassword(String oldPassword, boolean useEncryption, String currentPassword) {
		boolean passwordMatches = false;
		if (oldPassword != null) {
			if (useEncryption) {
				passwordMatches = HashCrypt.comparePassword(oldPassword,
						org.ofbiz.common.login.LoginServices.getHashType(), currentPassword);
			} else {
				passwordMatches = oldPassword.equals(currentPassword);
			}
		}
		if (!passwordMatches
				&& "true".equals(UtilProperties.getPropertyValue("security", "password.accept.encrypted.and.plain"))) {
			passwordMatches = currentPassword.equals(oldPassword);
		}
		return passwordMatches;
	}

	public static Map<String, Object> getLDAPSecurityGroups(DispatchContext ctx, Map<String, ?> context) {
		
		LDAPService ldapService = new LDAPService();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Map<String, Object> inputFields = (Map<String, Object>) context.get("inputFields");
		String ldapUserName = (String) inputFields.get("ldapUserName");
		String ldapPassword = (String) inputFields.get("ldapPassword");
		String ldapDomain = (String) inputFields.get("ldapDomain");
		String ldapEmailDomain = (String) inputFields.get("ldapEmailDomain");
		String ldapUrl = (String) inputFields.get("ldapUrl");
		String searchFilter = "(&(objectClass=user)(sAMAccountName=" + ldapUserName + "))";
		try {
		if (UtilValidate.isEmpty(ldapDomain) || UtilValidate.isEmpty(ldapEmailDomain)
				|| UtilValidate.isEmpty(ldapUrl)) {
			result.put("status", "ERROR");
			return result;
		}
				
		ldapService.init(ldapUserName, ldapPassword, ldapDomain, ldapEmailDomain, ldapUrl, searchFilter);
		Authentication authentication =  ldapService.ldapService();
		if(authentication.isAuthenticated()) {
		result.put("status","SUCCESS");
		//result.put("authentication", authentication);
		}else {
			result.put("status", "ERROR");
		}
		
		}catch (Exception e) {
			Debug.logError(e, "Could not authenticate user " + e.getMessage(),
					module);
			result.put("status", "ERROR");
		}
		return result;
		
	}
	
    public static Map<String, Object> createSecurityGroupFromPosition(DispatchContext ctx, Map<String, ?> context) {
        String partyId = (String) context.get("partyId");
        String userName = (String) context.get("userName");
        Delegator delegator = ctx.getDelegator();
        if (UtilValidate.isNotEmpty(partyId)) {
            try {
                Timestamp fromDate = UtilDateTime.nowTimestamp();
                GenericValue emplPositionFulfillemt = EntityQuery.use(delegator).from("EmplPositionFulfillment")
                    .where("partyId", partyId).filterByDate().orderBy("-fromDate").queryFirst();
                if (emplPositionFulfillemt != null && emplPositionFulfillemt.size() > 0) {
                    GenericValue emplPosition = EntityQuery.use(delegator).from("EmplPosition")
                        .where("emplPositionId", emplPositionFulfillemt.getString("emplPositionId")).queryOne();
                    if (emplPosition != null && emplPosition.size() > 0) {
                        String emplPositionTypeId = emplPosition.getString("emplPositionTypeId");
                        if (UtilValidate.isNotEmpty(emplPositionTypeId)) {
                            GenericValue roleType = EntityQuery.use(delegator).from("RoleType")
                                .where("roleTypeId", emplPositionTypeId).queryOne();
                            if (roleType != null && roleType.size() > 0) {
                                List < GenericValue > securityGroupRoleTypeAssoc = EntityQuery.use(delegator).from("SecurityGroupRoleTypeAssoc")
                                    .where("roleTypeId", emplPositionTypeId).queryList();
                                if (securityGroupRoleTypeAssoc != null && securityGroupRoleTypeAssoc.size() > 0) {
                                    EntityQuery securityGroup = EntityQuery.use(delegator).from("SecurityGroup")
                                        .where(EntityCondition.makeCondition("groupId", EntityOperator.IN, EntityUtil.getFieldListFromEntityList(securityGroupRoleTypeAssoc, "groupId", true)));
                                    List < String > groupIdList = EntityUtil.getFieldListFromEntityList(securityGroup.queryList(), "groupId", true);
                                    if (groupIdList != null && groupIdList.size() > 0) {
                                        EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND,
                                                EntityCondition.makeCondition("userLoginId",EntityOperator.EQUALS,userName),
                                                EntityCondition.makeCondition("groupId",EntityOperator.NOT_IN, groupIdList),
                                                EntityCondition.makeCondition("groupId",EntityOperator.LIKE,"DBS_%")
                                                );
                                        delegator.removeByCondition("UserLoginSecurityGroup", condition);
                                        for (String groupId: groupIdList) {
                                            List < GenericValue > userLoginSecurityGroup = EntityQuery.use(delegator).from("UserLoginSecurityGroup")
                                                .where("userLoginId", userName, "groupId", groupId)
                                                .filterByDate().queryList();
                                            if (userLoginSecurityGroup == null || userLoginSecurityGroup.size() < 1) {
                                                GenericValue userLoginSGMakeValue = delegator.makeValue("UserLoginSecurityGroup", UtilMisc.toMap("userLoginId", userName, "groupId", groupId));
                                                userLoginSGMakeValue.put("fromDate", fromDate);
                                                userLoginSGMakeValue.create();
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            } catch (Exception e) {
                Debug.logError("Exception in create security group From Position " + e.getMessage(), module);
            }
        }
        return ServiceUtil.returnSuccess();
    }
    public static String addCalendarOwnerRoleType(Delegator delegator, String partyId) {
    	if(UtilValidate.isNotEmpty(partyId)) {
    		try {
    			EntityQuery resultQuery = EntityQuery.use(delegator).from("SystemProperty").where("systemResourceId", "add_cal_owner_role");
    			List<String> resultList = EntityUtil.getFieldListFromEntityList(resultQuery.queryList(), "systemPropertyValue", true); 
    			if (UtilValidate.isNotEmpty(resultList)) {
    				GenericValue partyRole = EntityQuery.use(delegator).from("PartyRole")
    						.where(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId),
    								EntityCondition.makeCondition("roleTypeId",EntityOperator.IN, resultList)).queryFirst();
    				if(partyRole != null && partyRole.size() > 0) {
    					GenericValue partyRoleGv = EntityQuery.use(delegator).from("PartyRole")
    							.where("partyId", partyId, "roleTypeId", "CAL_OWNER").queryOne();
    					if(partyRoleGv == null || partyRoleGv.size() < 1) {
    						partyRoleGv = delegator.makeValue("PartyRole", UtilMisc.toMap("partyId", partyId, "roleTypeId", "CAL_OWNER"));
    						partyRoleGv.create();
    					}

    				}
    			} 
    		} catch(Exception e) {
    			Debug.logError("Exception in addCalendarOwnerRoleType " + e.getMessage(), module);
    			return "ERROR";
    		}
    	}
    	return "SUCCESS";
    }
}