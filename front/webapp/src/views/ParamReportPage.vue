<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
    <v-layout align-center justify-start column fill-height style="border: solid 0 blue">
        <v-container class="px-2 py-1">
            <v-layout class="mb-3" justify-start row>
                <v-card class="pa-3" style="width: 40%; min-width: 350px">
                    <v-flex class="mb-2 pr-2">
                        <v-layout class="lineText" row>
                            <v-flex align-self-center md6>ID формы отчета</v-flex>
                            <v-flex align-self-center md6>{{ mainInfo.id }}</v-flex>
                        </v-layout>
                        <v-divider></v-divider>
                        <v-layout class="lineText" row>
                            <v-flex align-self-center md6>Название</v-flex>
                            <v-flex align-self-center md6>{{ mainInfo.name }}</v-flex>
                        </v-layout>
                        <v-divider></v-divider>
                        <v-layout class="lineText" row>
                            <v-flex align-self-center md6>Полное название</v-flex>
                            <v-flex align-self-center md6 class="py-2">{{ mainInfo.full_name }}</v-flex>
                        </v-layout>
                        <v-divider></v-divider>
                        <v-layout class="lineText" row>
                            <v-flex align-self-center md6>Тип периода</v-flex>
                            <v-flex align-self-center md6>{{ mainInfo.period }}</v-flex>
                        </v-layout>
                    </v-flex>
                </v-card>
            </v-layout>
            <v-layout >
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
                        <v-btn class="icon-btn" color="primary" @click="getTableData">
                            <v-icon>refresh</v-icon>
                        </v-btn>

                        <v-btn color="primary" @click="createReport">Сформировать</v-btn>



                    </v-card-title>
                    <v-data-table-custom
                            :headers="headers"
                            :items="data"
                            :pagination.sync="pagination"
                            :loading="loadData"
                            :rows-per-page-items="pages"
                            class="mx-4"
                            :search="search"
                    >
                        <template v-slot:headers="props">
                            <tr>
                                <th v-for="header in props.headers"
                                    :key="header.text"
                                    :class="['column ', header.sortable ? 'sortable' : '', pagination.descending ? 'desc' : 'asc', header.value === pagination.sortBy ? 'active' : '', 'text-xs-' + header.align]"
                                    :style="{width: header.width, align: header.align}"
                                    @click="header.sortable ? changeSort(header.value) : ''"
                                >
                                    {{ header.text }}
                                    <v-icon v-if="header.sortable" small>arrow_upward</v-icon>
                                </th>
                            </tr>
                        </template>
                        <template v-slot:items="props">
                            <tr>
                                <td class="text-xs-left">{{ props.item.rep_nm }}</td>
                                <td class="text-xs-left">{{ props.item.msr_prd_tp | noData}}</td>
                                <td class="text-xs-left">{{ props.item.msr_prd_caption | noData}}</td>
                                <td class="text-xs-left">{{ props.item.format }}</td>
                                <td class="text-xs-left">{{ dateTableFormat(props.item.ppn_dttm) | noData}}</td>
                                <td class="text-xs-center">
                                    <div style="display: flex; justify-content: center;">
                                        <template v-if="true">
                                            <v-icon class="icon-btn" @click="openReport(props.item, props.item.format)">
                                                {{ props.item.format === 'pdf' ? 'mdi-file-pdf' :
                                                   props.item.format === 'xlsx' ? 'mdi-file-excel' :
                                                   props.item.format === 'docx' ? 'mdi-file-word' :
                                                  'mdi-file-xml'
                                                }}
                                            </v-icon>
                                        </template>
                                    </div>
                                </td>
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
                    </v-data-table-custom>
                </v-card>
            </v-layout>

            <v-dialog v-model="create.showMenu" max-width="600">
                <v-card>
                    <v-card-title class="headline">Сформировать параметризированный отчет</v-card-title>

                    <v-card-text>
                        <v-layout class="lineText" row>
                            <v-flex md6>Форма отчета</v-flex>
                            <v-flex align-left md6>{{mainInfo.name}}</v-flex>
                        </v-layout>
                        <v-layout class="lineText" row>
                            <v-flex align-self-center md6>Наименование отчета</v-flex>
                            <v-flex align-left md6><v-text-field v-model="create.file_name"></v-text-field></v-flex>
                        </v-layout>
                        <v-layout class="lineText" row>
                            <v-flex md6>Формат</v-flex>
                            <v-flex md6 class="py-2">
                                <v-radio-group v-model="create.format" row>
                                    <v-radio label="PDF" value="pdf"></v-radio>
                                    <v-radio label="XLSX" value="xlsx"></v-radio>
                                    <v-radio label="DOCX" value="docx"></v-radio>
                                </v-radio-group>
                            </v-flex>
                        </v-layout>
                    </v-card-text>

                    <v-card-actions>
                        <v-spacer></v-spacer>
                        <v-btn flat @click="create.showMenu = false">Отмена</v-btn>
                        <v-btn color="primary" flat @click="goToReport">Далее</v-btn>
                    </v-card-actions>
                </v-card>
            </v-dialog>

        </v-container>
    </v-layout>
</template>

<script>
    import moment from 'moment';
    import * as VueNotifications from "vue-notifications";
    import _ from 'lodash';

    export default {
        name: "ReportPage",
        components: {},

        data() {
            return {
                baseUrl:( (process.env.VUE_APP_BIRT_VIEWER_URL.startsWith('http'))?process.env.VUE_APP_BIRT_VIEWER_URL:(process.env.VUE_APP_BACKEND_URL+process.env.VUE_APP_BIRT_VIEWER_URL)),

                repId: null,
                mainInfo: {
                    name: "",
                    rptdesign: "",
                },

                create: {
                    showMenu: false,
                    file_name: "",
                    format: "pdf",
                },

                pagination: {
                    sortBy: 'ppn_dttm',
                    descending: true
                },
                pages: [10, 25, 50, 100],
                rowsPerPage: null,
                search: '',
                loadData: false,

                page: null,
                sortBy: null,
                descending: null,
                total: 0,

                params: [],
                headers: [
                    {text: 'Имя отчета', value: 'rep_nm', width: '30%', align: 'left', sortable: false},
                    {text: 'Тип периода', value: 'msr_prd_tp', width: '10%', align: 'left', sortable: true},
                    {text: 'Период', value: 'msr_prd_caption', width: '10%', align: 'left', sortable: true},
                    {text: 'Формат отчета', value: 'format', width: '10%', align: 'left', sortable: true},
                    {text: 'Дата и время формирования отчета', value: 'ppn_dttm', width: '20%', align: 'center', sortable: true},
                    {text: 'Действия', value: 'actions', width: '10%', align: 'center', sortable: false}
                ],

                data: [],
                //refreshData: false,
            }
        },

        filters: {
            noData(v) {
                return (v == null || v == '') ? '-' : v;
            }
        },

        notifications: {
            showSuccessMsg: {
                type: VueNotifications.types.success,
                title: '',
                message: '',
            },
            showErrorMsg: {
                type: VueNotifications.types.error,
                title: '',
                message: '',
            },
            showInfoMsg: {
                type: VueNotifications.types.info,
                title: '',
                message: ''
            }
        },

        watch: {
            pagination: {
                handler(v) {
                    this.getTableData();
                    this.page = v.page-1;
                    this.rowsPerPage = v.rowsPerPage;
                    this.sortBy = v.sortBy;
                    this.descending = v.descending;
                },
                deep: true
            },

            search() {
                this.pagination.page = 1;
                this.getTableData();
            },

            repId(v) {
                this.getMainInfo(v);
                this.getTableData();
            }
        },

        mounted() {
            this.repId = this.$route.params.rep_id;
        },

        updated() {
            this.repId = this.$route.params.rep_id;
        },

        computed: {
            optionsLink() {
                return this.createLink({
                    token: this.$store.getters.getToken,
                });
            },
            reportLink() {
                return this.createLink({
                    rep_tp_id: this.repId,
                    rep_nm: this.create.file_name,
                    format: this.create.format,
                    __format: this.create.format,
                    username: this.$store.getters.getUserName,
                    user: this.$store.getters.getFullName,
                    __report: this.mainInfo.rptdesign + '.rptdesign',
                });
            },
        },

        methods: {
            createReport() {
                this.create.showMenu = true;
            },

            goToReport() {
                this.$axios.options(this.optionsLink).then((resp) => {
                    this.create.showMenu = false;
                }).then(() => {
                    const link = document.createElement('a');
                    link.href = this.reportLink;
                    link.target = '_blank';
                    document.body.appendChild(link);
                    link.click();
                }).catch(error => {
                    console.log("error", error)
                })
            },

            createLink(params) {
                const url = this.baseUrl + "?";
                const query = Object.entries(params).map(([key, val]) => `${key}=${encodeURIComponent(val)}`).join('&');
                return url + query;
            },


            getTableData() {
                const [pageNumber, pageSize, sortingColumn, sortingDirection] =
                [this.pagination.page - 1, this.pagination.rowsPerPage, this.pagination.sortBy, this.pagination.descending];

                this.params = [];
                this.loadData = true;
                const params = {
                    pageSize: pageSize,
                    pageNumber: pageNumber,
                    sortingColumn: sortingColumn,
                    sortingDirection: this.sortTable(sortingDirection),
                    query: this.search ? this.search : null
                };

                this.$axios.get(this.$urls.PARAM_REPORT_TABLE + '/' + this.repId, {params})
                    .then(resp => {
                        this.data = resp.data.content;
                        this.total = resp.data.totalElements;
                        this.pagination.totalItems = resp.data.totalElements;
                        if (this.refreshData) {
                            this.getMainInfo(this.dirType);
                            //setTimeout(this.showRefreshSuccess, 2300); //ждем 3 секунды
                            this.showRefreshSuccess();
                        }
                    })
                    .catch(e => {
                        console.log('error: ', e);
                    })
                    .finally(() => {
                        this.loadData = false;
                    });
            },

            getMainInfo(id) {
                if (id) {
                    this.$axios.get(this.$urls.REPORT_MAIN_INFO + '/' + id)
                        .then(resp => {

                            _.merge(this.mainInfo, {
                                id: resp.data.rep_tp_id,
                                name: resp.data.shrt_nm,
                                full_name: resp.data.name,
                                period: resp.data.periods_label,
                                rptdesign: resp.data.rptdesign,
                            });
                            this.create.file_name = this.mainInfo.name;

                        })
                        .catch(e => {
                            console.log('error: ', e);
                        });
                }
                else {
                    this.mainInfo = {
                        id: 0,
                        name: 'Неизвестно',
                        full_name: 'Неизвестно',
                        change_dtm: null
                    }
                }
            },

            sortTable(descending) {
                if (descending != null) {
                    if (descending)
                        return 'DESC';
                    else return 'ASC'
                } else return descending;
            },

            changeSort (column) {
                if (this.pagination.sortBy === column) {
                    this.pagination.descending = !this.pagination.descending
                } else {
                    this.pagination.sortBy = column;
                    this.pagination.descending = false
                }
            },

            dateTableFormat(date) {
                if (date == null)
                    return date;
                else {
                    return moment(date).locale('ru').format('DD.MM.YY hh:mm');
                }
            },

            openReport(item, form) {
                console.log("open report ", item, form);
                this.$axios.get(this.$urls.GET_PARAMS_REPORT + '/' + item.repId, {responseType: 'arraybuffer'})
                    .then((response) => {
                        const url = window.URL.createObjectURL(new Blob([response.data]));
                        const link = document.createElement('a');
                        link.href = url;
                        const filename = 'Report.' + form;
                        link.setAttribute('download', filename);
                        document.body.appendChild(link);
                        link.click();
                        //link.close();
                    })
                    .catch(e => {
                        console.log('error: ', e);
                    });
            }
        }
    }
</script>

<style scoped>
    .text-style {
        text-align: center;
    }

    .lineText {
        width: 100%;
        min-height: 45px;
    }

</style>
<style>
    .icon-btn {
        padding: 3px !important;
        height: auto !important;
        cursor: pointer;
    }
    .icon-btn.v-btn {
        min-width: 0 !important;
    }
    .icon-btn .v-icon {
        font-size: 30px;
    }
</style>