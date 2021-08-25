package dev.ams.cloud.multitenant.config;

public class TenantContext {

	private static ThreadLocal<String> currentTenant = new InheritableThreadLocal<>();

	public static String getCurrentTenant() {
		return currentTenant.get();
	}

	public static void setCurrentTenant(String value) {
		currentTenant.set(value);
	}
	
	public static void clear() {
		currentTenant.remove();
	}
	
}
