/*********************************************************************
 * Copyright (c)  2019 Expleo Germany GmbH [and others].
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Expleo Germany GmbH
 **********************************************************************/

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
