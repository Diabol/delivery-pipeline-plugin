/*
This file is part of Delivery Pipeline Plugin.

Delivery Pipeline Plugin is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Delivery Pipeline Plugin is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Delivery Pipeline Plugin.
If not, see <http://www.gnu.org/licenses/>.
*/
package se.diabol.jenkins.pipeline;

public class PipelinePagination {
    private int totalCount = 0;
    private int currentPage = 1;
    private int prevPage = 0;
    private int nextPage = 0;
    private int totalPage = 0;
    private int pageSize = 10;
    private final String moveLink;


    public PipelinePagination(int currentPage, int totalCount, int pagingSize, String moveLink) {
        this.currentPage = currentPage;
        this.totalCount = totalCount;
        this.pageSize = pagingSize == 0 ? 10 : pagingSize;
        this.moveLink = moveLink;
    }

    public String getTag() {
        setPagination();
        StringBuilder sb = new StringBuilder();
        if (prevPage + 1 > 10) {
            setPrePage(sb);
        }
        setIndex(sb);
        if (totalPage > (prevPage + 10)) {
            setNextPage(sb);
        }
        return sb.toString();
    }

    private void setNextPage(StringBuilder sb) {
        sb.append("<a href='").append(moveLink)
                .append(nextPage).append("'>\n").append("Next")
                .append("</a>\n");
    }

    private void setPrePage(StringBuilder sb) {
        sb.append("<a href='").append(moveLink).append(prevPage).append("'>\n")
                .append("Prev").append("</a>\n");
    }

    private void setIndex(StringBuilder sb) {
        for (int count = prevPage + 1; count < nextPage && count <= totalPage; count++) {
            if (count == currentPage) {
                sb.append("<span");
                sb.append(" class='active_link'>\n");
                sb.append("<a>").append(count).append("</a>\n");
                sb.append("</span>\n");
            } else {
                sb.append("<a href='").append(moveLink).append(count).append("'>").append(count).append("</a>\n");
            }
        }
    }

    private void setPagination() {
        int current = (currentPage - 1) / 10 + 1;
        prevPage = (current - 1) * 10;
        nextPage = current * 10 + 1;
        totalPage = ((totalCount - 1) / pageSize) + 1;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }
}
