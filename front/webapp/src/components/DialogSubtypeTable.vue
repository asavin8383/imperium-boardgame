<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
    <v-dialog v-model="show" lazy persistent width="90%%">
        <v-card class="pa-3" style="width: 100%">
            <v-card-title>
                <v-flex md6>
                    <v-text-field
                            v-model="search"
                            append-icon="search"
                            label="Найти"
                            single-line
                            hide-details
                            style="width: 50%"
                    ></v-text-field>
                </v-flex>
                <v-spacer></v-spacer>
                <v-icon @click="closeCard">close</v-icon>
            </v-card-title>
            <v-data-table
                    :headers="headers"
                    :items="data"
                    :pagination.sync="pagination"
                    :total-items="pagination.totalItems"
                    :loading="loadData"
                    :rows-per-page-items="pages"
                    class="mx-4"
            >
                <template v-slot:items="props">
                    <tr @click="chooseSubtype(props.item)" class="clickable-row">
                        <td>{{ props.item.id | noData}}</td>
                        <td>{{ props.item.registryName | noData}}</td>
                        <td>{{ props.item.categoryName | noData}}</td>
                        <td>{{ props.item.violationName | noData}}</td>
                        <td>{{ dateTableFormat(props.item.cDate) | noData}}</td>
                    </tr>
                </template>
                <template v-slot:no-results>
                    <v-alert :value="true" color="warning" icon="warning">
                        По запросу "{{ search }}" ничего не найдено.
                    </v-alert>
                </template>
                <template v-slot:no-data v-if="!loadData">
                    <v-alert :value="true" color="warning" icon="warning">
                        Нет данных для отображения.
                    </v-alert>
                </template>
            </v-data-table>
        </v-card>
    </v-dialog>
</template>

<script>
    import moment from 'moment';

    export default {
        name: "DialogSubtypeTable",

        props: ['value'],


        data() {
            return {
                pagination: {
                    sortBy: 'id',
                },
                search: null,
                pages: [10, 25, 50, 100],
                page: null,
                rowsPerPage: null,
                sortBy: null,
                descending: null,
                loadData: false,

                total: 0,
                params: [],

                urlInfo: this.$urls.SUBTYPE_INFO,

                data: [],
                headers:  [
                    {text: 'ID', value: 'id'},
                    {text: 'Реестр', value: 'registryName'},
                    {text: 'Категория', value: 'categoryName'},
                    {text: 'Нарушение', value: 'violationName'},
                    {text: 'Дата и время последнего изменения', value: 'cDate'},
                ]
            }
        },

        filters: {
            noData(v) {
                return (v == null || v == '') ? '-' : v;
            }
        },

        computed: {
            show: {
                set: function (v) {
                    this.$emit('input', v)
                },
                get: function () {
                    return this.value;
                }
            }
        },

        watch: {
            pagination: {
                handler(v) {
                    this.getData(v.page - 1, v.rowsPerPage, v.sortBy, v.descending);
                    this.page = v.page - 1;
                    this.rowsPerPage = v.rowsPerPage;
                    this.sortBy = v.sortBy;
                    this.descending = v.descending;
                },
                deep: true
            },

            search() {
                this.pagination.page = 1;
                this.getData(this.pagination.page - 1, this.pagination.rowsPerPage, this.pagination.sortBy, this.pagination.descending);
            },
        },

        methods: {
            getData(pageNumber, pageSize, sortingColumn, sortingDirection) {
                console.log("getData");
                this.params = [];
                const config = () => ({
                    params: {
                        pageSize: pageSize,
                        pageNumber: pageNumber,
                        sortingColumn: sortingColumn,
                        sortingDirection: this.sortTable(sortingDirection)
                    }
                });

                const configSearch = () => ({params: {
                        pageSize: pageSize,
                        pageNumber: pageNumber,
                        sortingColumn: sortingColumn,
                        sortingDirection: this.sortTable(sortingDirection),
                        query: this.search
                    }});

                    this.search ? this.getDataRequest(this.urlInfo, configSearch) :
                        this.getDataRequest(this.urlInfo, config);
            },

            getDataRequest(url, config) {
                this.$axios.get(url, config())
                    .then(resp => {
                        this.data = resp.data.content;
                        this.total = resp.data.totalElements;
                        this.pagination.totalItems = resp.data.totalElements;
                    })
                    .catch(e => {
                        console.log('error: ', e);
                    })
                    .finally(() =>{
                        this.loadData = false;
                    });
            },

            sortTable(descending) {
                if (descending != null) {
                    if (descending)
                        return 'DESC';
                    else return 'ASC'
                } else return descending;
            },

            dateTableFormat(date) {
                                if (date == null)
                                    return date;
                                else {
                                    return moment(date).locale('ru').format('DD.MM.YY hh:mm');
                                }
/*                try {
                    return moment(date).locale('ru').format('DD.MM.YY hh:mm');
                }
                catch {
                    return date;
                }*/
            },

            statusFormat(s) {
                return s ? 'Работает' : 'Не работает'
            },

            closeCard() {
                this.show = false;
            },

            chooseSubtype(item) {
                console.log("subtype chosen (id " + item.origId + ")");
                this.$emit('linkSubtype', item);
                this.closeCard();
            }
        }

    }
</script>

<style>
    .clickable-row {
        cursor: pointer;
    }

</style>