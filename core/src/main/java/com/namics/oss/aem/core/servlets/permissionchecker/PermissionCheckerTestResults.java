package com.namics.oss.aem.core.servlets.permissionchecker;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class to provide an overview about all executed test cases.
 *
 * @author Mike Schmid
 */
@Data
@JsonPropertyOrder({ "allTestsSuccessful", "testsExecuted", "testsSuccessful", "testsFailed", "testResults" })
public class PermissionCheckerTestResults {

	@JsonSerialize
	public int getTestsExecuted() {
		return testResults.size();
	}

	@JsonSerialize
	public long getTestsSuccessful() {
		return testResults.stream().filter(PermissionCheckerTestResult::isSuccess).count();
	}

	@JsonSerialize
	public long getTestsFailed() {
		return getTestsExecuted() - getTestsSuccessful();
	}

	@JsonSerialize
	public boolean getAllTestsSuccessful() {
		return getTestsFailed() == 0;
	}

	private List<PermissionCheckerTestResult> testResults = new ArrayList<>();

}
