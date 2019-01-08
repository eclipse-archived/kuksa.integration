package org.eclipse.kuksa.testing.model;

public class Command {
	private final String command;
	private final String payload;

	Command(final String command, final String payload) {
		this.command = command;
		this.payload = payload;
	}

	public String getCommand() {
		return command;
	}

	public String getPayload() {
		return payload;
	}

}
