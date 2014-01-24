package com.sismics.music.core.event;

import java.util.List;

import com.google.common.base.Objects;

/**
 * Articles updated event.
 *
 * @author bgamard
 */
public class ArticleUpdatedAsyncEvent {
    /**
     * List of updated articles.
     */
    private List<Article> articleList;
    
    /**
     * Getter of articleList.
     *
     * @return articleList
     */
    public List<Article> getArticleList() {
        return articleList;
    }

    /**
     * Setter of articleList.
     *
     * @param articleList articleList
     */
    public void setArticleList(List<Article> articleList) {
        this.articleList = articleList;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("articles", (articleList != null ? articleList.size() : "0") + " articles")
                .toString();
    }
}