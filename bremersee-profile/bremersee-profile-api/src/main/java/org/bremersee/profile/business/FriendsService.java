/*
 * Copyright 2016 the original author or authors.
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

package org.bremersee.profile.business;

import org.bremersee.profile.model.RoleDto;

import java.util.Collection;
import java.util.Set;

/**
 * @author Christian Bremer
 */
public interface FriendsService {

    RoleDto getFriendsRole(String user);

    RoleDto createFriendsRole(String user);

    void deleteFriendsRole(String user);


    void updateFriends(String user, Collection<String> friends);

    void addFriends(String user, Collection<String> friends);

    void removeFriends(String user, Collection<String> friends);

    Set<String> getFriends(String user);


    Set<String> getUsersWhoHaveMeAsFriend(String user);

}
