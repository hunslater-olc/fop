/*
 * Copyright 2005 The Apache Software Foundation.
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

/* $Id$ */

package org.apache.fop.fo.properties;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.KeepProperty;

/**
 * Shorthand parser for page-break-before, page-break-after and page-break-inside.
 * Used to set the corresponding keep-* and break-* properties.
 */
public class PageBreakShorthandParser implements ShorthandParser {
    
    /**
     * @see org.apache.fop.fo.properties.ShorthandParser#getValueForProperty()
     */
    public Property getValueForProperty(int propId,
                                               Property property,
                                               PropertyMaker maker,
                                               PropertyList propertyList)
                    throws PropertyException {

        if (propId == Constants.PR_KEEP_WITH_PREVIOUS 
                || propId == Constants.PR_KEEP_WITH_NEXT
                || propId == Constants.PR_KEEP_TOGETHER) {
            if (property.getEnum() == Constants.EN_AVOID) {
                return maker.make(null, Constants.CP_WITHIN_PAGE, 
                            propertyList, "always", propertyList.getFObj());
            }
        } else if (propId == Constants.PR_BREAK_BEFORE
                || propId == Constants.PR_BREAK_AFTER) {
            switch (property.getEnum()) {
            case Constants.EN_ALWAYS:
                return new EnumProperty(Constants.EN_PAGE, "PAGE");
            case Constants.EN_LEFT:
                return new EnumProperty(Constants.EN_EVEN_PAGE, "EVEN_PAGE");
            case Constants.EN_RIGHT:
                return new EnumProperty(Constants.EN_ODD_PAGE, "ODD_PAGE");
            case Constants.EN_AVOID:
            default:
                //nop;
            }
        }
        return null;
    }

}