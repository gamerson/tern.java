/**
 *  Copyright (c) 2013-2014 Liferay, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Gregory Amerson <gregory.amerson@liferay.com> - initial API and implementation
 */
package tern.eclipse.ide.core;


/**
 * Tern file helper manager API.
 *
 */
public interface ITernFileHelperManager {

	/**
	 * Returns an array of all tern file helpers
	 * <p>
	 * A new array is returned on each call, so clients may store or modify the
	 * result.
	 * </p>
	 *
	 * @return the array of tern file helpers {@link ITernFileHelper}
	 */
	ITernFileHelper[] getTernFileHelpers();

}
