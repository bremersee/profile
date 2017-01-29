/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.profile.validation;

/**
 * @author Christian Bremer
 */
@SuppressWarnings("WeakerAccess")
public abstract class ValidatorConstants {

    @SuppressWarnings("PointlessBitwiseExpression")
    public static final int USER_REGISTRATION_TABLE_MASK = 1 << 0;

    public static final int USER_TABLE_MASK = 1 << 1;

    public static final int OAUTH2_CLIENT_TABLE_MASK = 1 << 2;

    public static final int CHANGE_EMAIL_TABLE_MASK = 1 << 3;

    public static final int USER_AND_OAUTH2_CLIENT_TABLE_MASK = USER_TABLE_MASK + OAUTH2_CLIENT_TABLE_MASK;

    public static final int ALL_TABLE_MASK = USER_REGISTRATION_TABLE_MASK
            + USER_TABLE_MASK
            + OAUTH2_CLIENT_TABLE_MASK
            + CHANGE_EMAIL_TABLE_MASK;

    /**
     * Never construct.
     */
    private ValidatorConstants() {
        super();
    }

}
