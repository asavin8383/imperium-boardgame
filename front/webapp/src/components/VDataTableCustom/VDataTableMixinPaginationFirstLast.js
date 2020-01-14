
import $ from 'jquery'


export default {
    props: ['show_first_last_page'],

    data() {
        return {
        };
    },

    computed: {
        pages(){
            return this.pagination ? (this.pagination.rowsPerPage && this.totalItems ? Math.ceil(this.totalItems/this.pagination.rowsPerPage) : 0) : 0;
        },
        currentPage(){
            return this.pagination ? this.pagination.page : 1;
        },
        enabledFirst(){
            return (this.pages && this.currentPage > 1);
        },
        enabledLast(){
            return (this.pages && this.currentPage < this.pages);
        }
    },

    methods: {
        goToFirst(){
            if (this.pages){
                this.pagination.page = 1;
            }
        },
        goToLast(){
            if (this.pages){
                this.pagination.page = this.pages;
            }
        }
    },

    updated () {
        const selectorFirst = ".v-btn-first";
        const selectorLast = ".v-btn-last";
        this.$nextTick(function () {
            let $el = $(this.$el);
            let $prev = $el.find(".v-btn").eq(0);
            let $next = $el.find(".v-btn").eq(1);
            if (!$prev.length || !$next.length){
                return;
            }

            let $first = $el.find(selectorFirst);
            let $last = $el.find(selectorLast);

            if (!$first.length){
                let $content = $(
                    '<button class="v-btn-first v-btn v-btn--flat v-btn--icon theme--light">' +
                    '<div class="v_btn__content">' +
                    '<i aria-hidden="true" class="v-icon material-icons theme--light">first_page</i>' +
                    '</div>' +
                    '</button>'
                );
                $content.click(this.goToFirst);
                $prev.before($content);
                $first = $el.find(selectorFirst);
                this.__$first = $first;
            }
            if (!$last.length){
                let $content = $(
                    '<button class="v-btn-last v-btn v-btn--flat v-btn--icon theme--light">' +
                    '<div class="v_btn__content">' +
                    '<i aria-hidden="true" class="v-icon material-icons theme--light">last_page</i>' +
                    '</div>' +
                    '</button>'
                );
                $content.click(this.goToLast);
                $next.after($content);
                $last = $el.find(selectorLast);
                this.__$last = $last;
            }

            if (this.show_first_last_page || this.show_first_last_page == null){
                this.__$first.removeClass("hidden");
                this.__$last.removeClass("hidden");
            }
            else {
                this.__$first.addClass("hidden");
                this.__$last.addClass("hidden");
            }

            if (this.enabledFirst)
                this.__$first.removeClass("v-btn--disabled");
            else
                this.__$first.addClass("v-btn--disabled");

            if (this.enabledLast)
                this.__$last.removeClass("v-btn--disabled");
            else
                this.__$last.addClass("v-btn--disabled");
        });

    }
};

