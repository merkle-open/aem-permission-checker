package com.namics.oss.aem.core.servlets.permissionchecker;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Permission Checker Test Result.
 * Contains information about one executed permission check test case.
 *
 * @author Mike Schmid
 */
@Data
public class PermissionCheckerTestResult {

	private String name;
	private boolean success = true;
	private List<String> errors = new ArrayList<>();

}
