<template>
    <v-card flat class="filters-card mx-3">
        <v-card-text>
<!--            <v-btn @click="addFilterRow">Добавить +</v-btn>-->
            <v-layout align-start justify-space-between row class="pa-0">
                <v-flex md8>
                    <v-layout column class="cube">
                        <v-layout row>
                            <v-btn icon class=" ma-1 left" color="primary" title="Добавить" @click="addFilterRow">
                                <v-icon>add</v-icon>
                            </v-btn>
                        </v-layout>
                        <v-layout column>
                            <template v-for="(filter, idx) in filters">
                                <v-layout row justify-start class="mt-1">
                                    <v-flex md5 xs4 class="filter-row-element" style="padding-right: 5%">
                                        <v-select
                                                v-model="filter.field"
                                                class="areaTextSize"
                                                :items="computeFieldsList(filter.field)"
                                                item-text="label"
                                                item-value="field"
                                                hide-details
                                                prepend-icon="mdi-filter"
                                                placeholder="Выберите поле фильтра">
                                        </v-select>
                                    </v-flex>
                                    <v-flex md5 xs6 class="filter-row-element pr-3">
                                        <v-autocomplete v-if="filterType(filter.field) === 'list'"
                                                        class="areaTextSize"
                                                        placeholder="Выберите значение фильтра"
                                                        v-model="filter.value"
                                                        :items="valuesByField(filter.field)"
                                                        item-text="name"
                                                        item-value="value"
                                                        color="primary"
                                                        multiple
                                        ></v-autocomplete>
                                        <v-text-field
                                                v-if="filterType(filter.field) === 'text'"
                                                v-model="filter.value"
                                                class="areaTextSize"
                                                placeholder="Введите значение фильтра"></v-text-field>
                                        <v-text-field
                                                v-if="!filterType(filter.field)"
                                                v-model="filter.value"
                                                class="areaTextSize"
                                                :disabled="true"
                                                placeholder="Введите значение фильтра"></v-text-field>

                                    </v-flex>
                                    <v-flex md2 xs2 class="cube pa-0 pl-2">
                                        <!--                        <v-btn @click="removeFilterRow(idx)">
                                                                    удалить
                                                                </v-btn>-->
                                        <v-btn icon class=" ma-1 left" color="error" title="Удалить" @click="removeFilterRow(idx)">
                                            <v-icon>remove</v-icon>
                                        </v-btn>
                                    </v-flex>
                                </v-layout>
                            </template>
                        </v-layout>
                    </v-layout>
                </v-flex>
            </v-layout>
        </v-card-text>
        <v-card-actions class="pa-3">
            <v-spacer></v-spacer>
            <v-btn flat @click="clearFilters">Очистить фильтры</v-btn>
            <v-btn class="primary" @click="activateFilters">Применить</v-btn>
        </v-card-actions>
    </v-card>
</template>

<script>
    import {Status} from "../consts";
    let StatusMap = {};
    Status.forEach(s => {
        StatusMap[s.value] = s.name;
    });


    export default {
        props: ['params'],

        data() {
            return {
                filters: [],

                allFields: [
                    {
                        field: 'checkUnitTypes',
                        label: 'Тип ресурса',
                        serverData: true,
                        type: "list",
                        url: this.$urls.CHECK_UNIT_TYPES,
                        urlParams: ["arrangementId"]
                    },
                    {
                        field: 'checkUnitJobResults',
                        label: 'Результат',
                        serverData: true,
                        type: "list",
                        url: this.$urls.ARRANGEMENT_RESULTS,
                        urlParams: ["arrangementId"],
                        valueReplacement: StatusMap
                    }
                ],
                valueLists: []
            }
        },

        mounted() {
            this.loadFilterData();
        },

        watch: {
            params(){
                this.loadFilterData();
            }
        },

        computed: {
/*            allFields() {
                return this.dirData ? Object.keys(this.dirData) : null;
            }*/
        },

        methods: {
            loadFilterData(){
                let promiseArr = [];
                let self = this;
                this.allFields.forEach(item => {
                    if (item.serverData) {
                        promiseArr.push(
                            self.$axios.get(this.getUrlByField(item.field), this.getUrlParamsByField(item.field))
                                .then(resp => {
                                    return {
                                        fieldType: item.field,
                                        values: this.replaceValues(item.field, resp.data)
                                    }
                                }));
                    }
                });
                Promise.all(promiseArr)
                    .then(resultsArr => {
                        self.valueLists = resultsArr;
                    });
            },

            getFieldItem(field){
                let res = this.allFields.filter(item => item.field === field);
                return res.length > 0 ? res[0] : null;
            },

            getUrlParamsByField(field){
                let item = this.getFieldItem(field);
                if (!item)
                    return null;
                let params = {};
                item.urlParams.forEach(p => {
                    let argParams = this.params || {};
                    params[p] = argParams[p] == null ? "" : argParams[p];
                });
                return {params};
            },

            getUrlByField(field){
                let item = this.getFieldItem(field);
                return item ? item.url : null;
            },

            replaceValues(field, values){
                let item = this.getFieldItem(field);
                let valueReplacement = item ? item.valueReplacement||{} : {};
                return (values || []).map(v => ({
                    name: valueReplacement[v] || v,
                    value: v
                }));
            },


            filterType(field){
                let item = this.getFieldItem(field);
                return item ? (item.type||'text') : null;
            },

            valuesByField(field){
                let res = this.valueLists.find(x => x.fieldType === field) || {values: []};
                return res.values;
            },

            addFilterRow() {
                this.filters.push({
                    field: null,
                    value: null,
                    type: null
                })
            },

            removeFilterRow(idx) {
                this.filters.splice(idx, 1);
            },

            clearFilters() {
                this.filters = [];
            },

            activateFilters() {
                let filtersData =  {};
                filtersData.filters = [...this.filters];
                filtersData.filters = filtersData.filters.filter(item => !!item.field);
                filtersData.isEmpty = filtersData.filters.length === 0;
                this.$emit('filterSearch', filtersData);
            },

            computeFieldsList(field) {
                let temp = [...this.allFields];
                this.filters.forEach(filter => {
                    let idx = temp.indexOf(filter.field);
                    if (idx !== -1) temp.splice(idx, 1);
                });
                if (field) temp.push(field);
                return temp
            }
        }
    }
</script>

<style>
    .v-card.filters-card.v-sheet {
        /*border: 1px solid #1976d2;*/
        border: 1px solid rgba(0,0,0,0.54);
    }

    .filter-row-element {
/*        padding-right: 5%;*/
    }

    .number-text-field.v-text-field {
        /*width: 40px;*/
    }


</style>