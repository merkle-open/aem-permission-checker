package com.namics.oss.aem.core.servlets.permissionchecker;

import lombok.Data;

import java.util.List;

/**
 *
 * Model class (POJO) of a received permission checker configuration.
 *
 * @author Mike Schmid
 */
@Data
public class PermissionTestCase {

	private String name;
	private List<String> paths;
	private List<String> users;
	private List<String> allow;
	private List<String> deny;

}
