package com.namics.oss.aem.core.servlets.permissionchecker;

import org.apache.commons.lang3.StringUtils;

import javax.jcr.*;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;

/**
 * Checks user permissions using an admin session.
 *
 * @author Mike Schmid
 */
public class PermissionChecker {

	private static final char[] NO_PASSWORD = StringUtils.EMPTY.toCharArray();

	private final Session adminSession;

	/**
	 * Creates a new permission checker instance using an admin session.
	 *
	 * @param adminSession the admin session used to check permissions of other users
	 */
	public PermissionChecker(final Session adminSession) {
		this.adminSession = adminSession;
	}

	/**
	 * Executes permission checks based on the passed permissionsTestCases.
	 *
	 * @param permissionTestCases the test case configuration
	 * @return a {@link PermissionCheckerTestResults} instance
	 * @throws RepositoryException if an exception occures while accessing the repository
	 */
	public PermissionCheckerTestResults checkPermissions(final PermissionTestCase[] permissionTestCases) throws RepositoryException {

		final PermissionCheckerTestResults allTestResults = new PermissionCheckerTestResults();

		for (PermissionTestCase permissionTestCase : permissionTestCases) {
			final PermissionCheckerTestResult testResult = executeTestCase(permissionTestCase, adminSession);
			allTestResults.getTestResults().add(testResult);
		}

		return allTestResults;
	}

	private PermissionCheckerTestResult executeTestCase(
			final PermissionTestCase permissionTestCase, Session adminSession) throws RepositoryException {

		final PermissionCheckerTestResult testResult = new PermissionCheckerTestResult();
		testResult.setName(permissionTestCase.getName());

		for (String user : permissionTestCase.getUsers()) {

			try {
				final Session userSession = adminSession.impersonate(new SimpleCredentials(user, NO_PASSWORD));
				final AccessControlManager accessControlManager = userSession.getAccessControlManager();

				for (String path : permissionTestCase.getPaths()) {

					for (String allowActions : permissionTestCase.getAllow()) {
						checkPermission(user, accessControlManager, path, allowActions, true, testResult);
					}

					for (String denyActions : permissionTestCase.getDeny()) {
						checkPermission(user, accessControlManager, path, denyActions, false, testResult);
					}

				}

			} catch (LoginException e){
				testResult.setSuccess(false);
				testResult.getErrors().add("Could not login user '" + user + "'");
			}
		}

		return testResult;

	}

	private void checkPermission(
			String user, AccessControlManager accessControlManager, String path, String privilege, boolean expected, PermissionCheckerTestResult testResult)
			throws RepositoryException {

		final Privilege privilegeToCheck = accessControlManager.privilegeFromName(PrivilegeMapper.getMappedPrivilege(privilege));

		String additionalErrorInfo = StringUtils.EMPTY;
		boolean actualPermission = false;
		try {
			actualPermission = accessControlManager.hasPrivileges(path, new Privilege[] { privilegeToCheck });
		} catch (PathNotFoundException e){
			additionalErrorInfo = " (or path might not exist)";
		}

		if (expected != actualPermission) {
			testResult.setSuccess(false);

			final String errorMessage = String
					.format("Failed! User: %s, Path: %s, Action: %s, Expected: %s, But was: %s %s", user, path, privilege, expected, actualPermission, additionalErrorInfo);
			testResult.getErrors().add(errorMessage);

		}

	}

}
