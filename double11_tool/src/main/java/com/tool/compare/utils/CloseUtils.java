package com.tool.compare.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;

@Slf4j
public class CloseUtils {

	public static void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				log.error("关闭错误", e);
			}
		}
	}

	public static void close(Closeable... closeables) {
		for (Closeable closeable : closeables) {
			close(closeable);
		}
	}

	public static void close(AutoCloseable... autoCloseables) {
		for (AutoCloseable closeable : autoCloseables) {
			close(closeable);
		}
	}

	public static void close(AutoCloseable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				log.error("关闭错误", e);
			}
		}
	}

}
