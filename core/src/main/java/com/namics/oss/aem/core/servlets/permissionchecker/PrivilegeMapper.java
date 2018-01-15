package com.namics.oss.aem.core.servlets.permissionchecker;

import com.day.cq.replication.Replicator;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.security.Privilege;

/**
 * Helper class to map basic permissions Strings to fully qualified jcr privileges.
 *
 * For example:
 * read -> {http://www.jcp.org/jcr/1.0}read
 *
 * @author Mike Schmid
 */
public final class PrivilegeMapper {

	private PrivilegeMapper() {throw new AssertionError("PrivilegeMapper is not meant to be instantiated!"); }

	private static final String[] SEARCH_LIST = new String[] {
			"read",
			"write",
			"modifyProperties",
			"addChildNodes",
			"removeNode",
			"removeChildNodes",
			"readAccessControl",
			"modifyAccessControl",
			"lockManagement",
			"versionManagement",
			"nodeTypeManagement",
			"retentionManagement",
			"lifecycleManagement",
			"all",
			"replicate" };

	private static final String[] REPLACE_LIST = new String[] {
			Privilege.JCR_READ,
			Privilege.JCR_WRITE,
			Privilege.JCR_MODIFY_PROPERTIES,
			Privilege.JCR_ADD_CHILD_NODES,
			Privilege.JCR_REMOVE_NODE,
			Privilege.JCR_REMOVE_CHILD_NODES,
			Privilege.JCR_READ_ACCESS_CONTROL,
			Privilege.JCR_MODIFY_ACCESS_CONTROL,
			Privilege.JCR_LOCK_MANAGEMENT,
			Privilege.JCR_VERSION_MANAGEMENT,
			Privilege.JCR_NODE_TYPE_MANAGEMENT,
			Privilege.JCR_RETENTION_MANAGEMENT,
			Privilege.JCR_LIFECYCLE_MANAGEMENT,
			Privilege.JCR_ALL,
			Replicator.REPLICATE_PRIVILEGE };

	/**
	 * Maps the passed privilege to a fully qualified jcr privilege.
	 *
	 * For example:
	 * read -> {http://www.jcp.org/jcr/1.0}read
	 *
	 * Unsupported privileges will not be mapped and will be returned unchanged:
	 * test -> test
	 *
	 * @param privilege the privilege to be mapped
	 * @return a mapped privilege String
	 */
	public static String getMappedPrivilege(final String privilege) {
		return StringUtils.replaceEach(privilege, SEARCH_LIST, REPLACE_LIST);
	}

}
